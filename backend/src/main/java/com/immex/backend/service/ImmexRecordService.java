package com.immex.backend.service;

import com.immex.backend.model.AuditEvent;
import com.immex.backend.model.DashboardInventorySummary;
import com.immex.backend.model.ImmexCloseReport;
import com.immex.backend.model.ImmexDashboardSummary;
import com.immex.backend.model.ImmexRecord;
import com.immex.backend.model.ImmexRecordRequest;
import com.immex.backend.model.PepsConsumptionRequest;
import com.immex.backend.model.PepsOperationResult;
import com.immex.backend.repository.ImmexRecordRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ImmexRecordService {
  private final ImmexRecordRepository repository;
  private final AuditEventService auditEventService;
  private final LoteMovimientoService loteMovimientoService;
  private final InventoryDashboardService inventoryDashboardService;

  public ImmexRecordService(ImmexRecordRepository repository, AuditEventService auditEventService, LoteMovimientoService loteMovimientoService, InventoryDashboardService inventoryDashboardService) {
    this.repository = repository;
    this.auditEventService = auditEventService;
    this.loteMovimientoService = loteMovimientoService;
    this.inventoryDashboardService = inventoryDashboardService;
  }

  public List<ImmexRecord> findAll(String razonSocial) {
    return repository.findAll().stream()
        .filter(record -> matchesCompany(record, razonSocial))
        .toList();
  }

  public ImmexRecord findById(String id) {
    return repository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado"));
  }

  public ImmexRecord create(ImmexRecordRequest request) {
    validateDuplicate(request, null);
    validateAuditorAndNotes(request.getAuditor(), request.getNotas());
    validateNonNegativeQuantity(request.getCantidad());
    Instant now = Instant.now();
    ImmexRecord record = new ImmexRecord(
        UUID.randomUUID().toString(),
        request.getPedimento().trim(),
        request.getMaterial().trim(),
        request.getProveedor().trim(),
        request.getMovimiento().trim(),
        trimToNull(request.getRazonSocial()),
        trimToNull(request.getRfc()),
        trimToNull(request.getPartida()),
        trimToNull(request.getUnidad()),
        trimToNull(request.getRegimenAduanero()),
        request.getCantidad(),
        request.getValorUnitario(),
        request.getFechaIngreso(),
        request.getFechaVencimiento(),
        request.getStatus().trim(),
        Boolean.TRUE.equals(request.getActivo()),
        trimToNull(request.getNotas()),
        trimToNull(request.getAuditor()),
        now,
        now);
    ImmexRecord saved = repository.save(record);
    auditEventService.record(
        "ImmexRecord",
        saved.getId(),
        "CREATE",
        actorFrom(request.getAuditor()),
        "Alta de movimiento",
        "Pedimento " + saved.getPedimento() + " / " + saved.getMaterial() + " / " + saved.getRazonSocial());
    return saved;
  }

  public ImmexRecord update(String id, ImmexRecordRequest request) {
    validateDuplicate(request, id);
    validateAuditorAndNotes(request.getAuditor(), request.getNotas());
    validateNonNegativeQuantity(request.getCantidad());
    ImmexRecord existing = findById(id);
    existing.setPedimento(request.getPedimento().trim());
    existing.setMaterial(request.getMaterial().trim());
    existing.setProveedor(request.getProveedor().trim());
    existing.setMovimiento(request.getMovimiento().trim());
    existing.setRazonSocial(trimToNull(request.getRazonSocial()));
    existing.setRfc(trimToNull(request.getRfc()));
    existing.setPartida(trimToNull(request.getPartida()));
    existing.setUnidad(trimToNull(request.getUnidad()));
    existing.setRegimenAduanero(trimToNull(request.getRegimenAduanero()));
    existing.setCantidad(request.getCantidad());
    existing.setValorUnitario(request.getValorUnitario());
    existing.setFechaIngreso(request.getFechaIngreso());
    existing.setFechaVencimiento(request.getFechaVencimiento());
    existing.setStatus(request.getStatus().trim());
    existing.setActivo(Boolean.TRUE.equals(request.getActivo()));
    existing.setNotas(trimToNull(request.getNotas()));
    existing.setAuditor(trimToNull(request.getAuditor()));
    existing.setUpdatedAt(Instant.now());
    ImmexRecord saved = repository.save(existing);
    auditEventService.record(
        "ImmexRecord",
        saved.getId(),
        "UPDATE",
        actorFrom(request.getAuditor()),
        "Actualización de movimiento",
        "Pedimento " + saved.getPedimento() + " / " + saved.getMaterial() + " / " + saved.getRazonSocial());
    return saved;
  }

  public void delete(String id) {
    ImmexRecord existing = findById(id);
    repository.deleteById(id);
    auditEventService.record(
        "ImmexRecord",
        existing.getId(),
        "DELETE",
        existing.getAuditor(),
        "Eliminación de registro",
        "Pedimento " + existing.getPedimento() + " / " + existing.getMaterial() + " / " + existing.getRazonSocial());
  }

  public ImmexDashboardSummary summary(String razonSocial) {
    List<ImmexRecord> records = findAll(razonSocial);
    ImmexRecord pepsCandidate = records.stream()
        .filter(record -> record.isActivo() && !"Cerrado".equalsIgnoreCase(record.getStatus()))
        .sorted(Comparator.comparing(ImmexRecord::getFechaIngreso, Comparator.nullsLast(Comparator.naturalOrder())))
        .findFirst()
        .orElse(null);

    long riskTotal = 0L;
    long expiringSoonCount = 0L;
    long alert90Count = 0L;
    long alert60Count = 0L;
    long alert30Count = 0L;
    long entryCount = 0L;
    long consumptionCount = 0L;
    long exportCount = 0L;
    long closeReadyCount = 0L;

    for (ImmexRecord record : records) {
      if (!isRiskRelevant(record)) {
        continue;
      }
      long risk = lineRisk(record).longValue();
      riskTotal += risk;
      long days = daysLeft(record);
      if (days <= 60) expiringSoonCount++;
      if (days <= 90 && days > 60) alert90Count++;
      if (days <= 60 && days > 30) alert60Count++;
      if (days <= 30) alert30Count++;
      if ("Entrada".equalsIgnoreCase(record.getMovimiento())) entryCount++;
      if ("Consumo".equalsIgnoreCase(record.getMovimiento())) consumptionCount++;
      if ("Exportación".equalsIgnoreCase(record.getMovimiento())) exportCount++;
      if (record.isActivo() || "Cerrado".equalsIgnoreCase(record.getStatus())) closeReadyCount++;
    }

    return new ImmexDashboardSummary(records.size(), riskTotal, expiringSoonCount, alert90Count, alert60Count, alert30Count, entryCount, consumptionCount, exportCount, closeReadyCount, pepsCandidate);
  }

  public ImmexCloseReport buildCloseReport(String razonSocial) {
    List<ImmexRecord> records = findAll(razonSocial);
    long openRecords = records.stream().filter(record -> record.isActivo() && !"Cerrado".equalsIgnoreCase(record.getStatus())).count();
    long expiringSoonCount = records.stream().filter(record -> daysLeft(record) <= 30).count();
    DashboardInventorySummary inventorySummary = inventoryDashboardService.inventory(razonSocial);
    BigDecimal saldoTotal = inventorySummary == null ? BigDecimal.ZERO : inventorySummary.saldoTotalMxn();
    BigDecimal montoEnRiesgo = inventorySummary == null ? BigDecimal.ZERO : inventorySummary.montoVencidoMxn().add(inventorySummary.montoPorVencerMxn());
    ImmexRecord pepsCandidate = records.stream()
        .filter(record -> record.isActivo())
        .sorted(Comparator.comparing(ImmexRecord::getFechaIngreso, Comparator.nullsLast(Comparator.naturalOrder())))
        .findFirst()
        .orElse(null);

    List<String> warnings = new java.util.ArrayList<>();
    if (openRecords > 0) {
      warnings.add("Hay movimientos abiertos pendientes de cierre.");
    }
    if (expiringSoonCount > 0) {
      warnings.add("Existen materiales dentro de la ventana crítica de 30 días.");
    }
    if (pepsCandidate == null) {
      warnings.add("No hay candidato PEPS disponible.");
    }

    long exceptionsPending = records.stream().filter(this::isRiskRelevant).count();
    List<com.immex.backend.model.ClosePendingItem> pendingItems = new java.util.ArrayList<>();
    pendingItems.add(new com.immex.backend.model.ClosePendingItem("movimientos-abiertos", "Movimientos abiertos", openRecords, "/dashboard/ledger"));
    pendingItems.add(new com.immex.backend.model.ClosePendingItem("excepciones-sin-resolver", "Excepciones sin resolver", exceptionsPending, "/dashboard/risk"));

    String estado;
    if (openRecords == 0 && exceptionsPending == 0) {
      estado = "Firmado";
    } else if (openRecords > 0 || exceptionsPending > 0) {
      estado = warnings.isEmpty() ? "En revisión" : "Abierto";
    } else {
      estado = "Abierto";
    }

    return new ImmexCloseReport(Instant.now(), records.size(), openRecords, expiringSoonCount, saldoTotal, montoEnRiesgo, pepsCandidate, warnings, pendingItems, estado);
  }

  public PepsOperationResult simulatePeps(PepsConsumptionRequest request) {
    validateReasonAndAuditor(request.getReason(), request.getAuditor());
    var movimientos = loteMovimientoService.simularPeps(
        request.getMaterial().trim(),
        request.getQuantity(),
        request.getMaterial().trim(),
        actorFrom(request.getAuditor()),
        request.getReason());
    int processed = movimientos.stream().mapToInt(movimiento -> movimiento.getCantidad() == null ? 0 : movimiento.getCantidad()).sum();
    boolean completed = processed == request.getQuantity();
    String message = completed
        ? "Simulación lista: no se guardó nada"
        : "Simulación parcial: no habría inventario suficiente para completar la solicitud";
    auditEventService.record(
        "Descarga",
        request.getMaterial(),
        "PEPS_SIMULATE",
        actorFrom(request.getAuditor()),
        request.getReason(),
        "Solicitado=" + request.getQuantity() + ", simulado=" + processed + ", movimientos=" + movimientos.size());
    return new PepsOperationResult(request.getMaterial(), request.getQuantity(), processed, completed, message, Instant.now(), movimientos);
  }

  public PepsOperationResult applyPeps(PepsConsumptionRequest request) {
    validateReasonAndAuditor(request.getReason(), request.getAuditor());
    var movimientos = loteMovimientoService.consumirPeps(
        request.getMaterial().trim(),
        request.getQuantity(),
        request.getMaterial().trim(),
        actorFrom(request.getAuditor()),
        request.getReason());
    int processed = movimientos.stream().mapToInt(movimiento -> movimiento.getCantidad() == null ? 0 : movimiento.getCantidad()).sum();
    boolean completed = processed == request.getQuantity();
    String message = completed
        ? "Consumo PEPS aplicado correctamente"
        : "Consumo parcial: no hubo inventario suficiente para completar la solicitud";
    auditEventService.record(
        "Descarga",
        request.getMaterial(),
        "PEPS_APPLY",
        actorFrom(request.getAuditor()),
        request.getReason(),
        "Solicitado=" + request.getQuantity() + ", procesado=" + processed + ", movimientos=" + movimientos.size());
    return new PepsOperationResult(request.getMaterial(), request.getQuantity(), processed, completed, message, Instant.now(), movimientos);
  }

  public PepsOperationResult consumePeps(PepsConsumptionRequest request) {
    return applyPeps(request);
  }

  public PepsOperationResult revertPeps(PepsConsumptionRequest request) {
    validateReasonAndAuditor(request.getReason(), request.getAuditor());
    var movimientos = loteMovimientoService.revertirPeps(
        request.getMaterial().trim(),
        request.getQuantity(),
        request.getMaterial().trim(),
        actorFrom(request.getAuditor()),
        request.getReason());
    int processed = movimientos.stream().mapToInt(movimiento -> movimiento.getCantidad() == null ? 0 : movimiento.getCantidad()).sum();
    boolean completed = processed == request.getQuantity();
    String message = completed
        ? "PEPS revertido correctamente"
        : "Reversión parcial: no se pudo devolver todo el saldo solicitado";
    auditEventService.record(
        "Descarga",
        request.getMaterial(),
        "PEPS_REVERT",
        actorFrom(request.getAuditor()),
        request.getReason(),
        "Solicitado=" + request.getQuantity() + ", revertido=" + processed + ", movimientos=" + movimientos.size());
    return new PepsOperationResult(request.getMaterial(), request.getQuantity(), processed, completed, message, Instant.now(), movimientos);
  }

  public List<AuditEvent> listAuditEvents() {
    return auditEventService.findAll();
  }

  private BigDecimal lineRisk(ImmexRecord record) {
    if (!isRiskRelevant(record) || record.getCantidad() == null || record.getValorUnitario() == null) {
      return BigDecimal.ZERO;
    }
    return record.getValorUnitario().multiply(BigDecimal.valueOf(record.getCantidad()));
  }

  private long daysLeft(ImmexRecord record) {
    if (record.getFechaVencimiento() == null) {
      return Long.MAX_VALUE;
    }
    return ChronoUnit.DAYS.between(LocalDate.now(), record.getFechaVencimiento());
  }

  private boolean isRiskRelevant(ImmexRecord record) {
    if (record == null) return false;
    String status = trimToNull(record.getStatus());
    String movimiento = trimToNull(record.getMovimiento());
    if (status != null && "Cerrado".equalsIgnoreCase(status)) return false;
    if (movimiento != null && "Exportación".equalsIgnoreCase(movimiento)) return false;
    if (!record.isActivo()) return false;
    return true;
  }

  private boolean matchesCompany(ImmexRecord record, String razonSocial) {
    if (razonSocial == null || razonSocial.isBlank() || "all".equalsIgnoreCase(razonSocial)) {
      return true;
    }
    String recordCompany = trimToNull(record.getRazonSocial());
    return recordCompany != null && recordCompany.equalsIgnoreCase(razonSocial.trim());
  }

  private void validateDuplicate(ImmexRecordRequest request, String currentId) {
    String pedimento = request.getPedimento().trim();
    String material = request.getMaterial().trim();
    String partida = trimToNull(request.getPartida());
    String rfc = trimToNull(request.getRfc());
    boolean duplicate = repository.findAll().stream().anyMatch(record ->
        (currentId == null || !currentId.equals(record.getId()))
            && pedimento.equalsIgnoreCase(record.getPedimento())
            && material.equalsIgnoreCase(record.getMaterial())
            && equalsIgnoreCase(partida, record.getPartida())
            && equalsIgnoreCase(rfc, record.getRfc()));
    if (duplicate) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un registro para ese pedimento, partida y material");
    }
  }

  private void validateNonNegativeQuantity(Integer quantity) {
    if (quantity == null || quantity <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor a cero");
    }
  }

  private void validateAuditorAndNotes(String auditor, String notas) {
    if (auditor == null || auditor.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario es obligatorio");
    }
    if (notas == null || notas.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El motivo es obligatorio");
    }
  }

  private void validateReasonAndAuditor(String reason, String auditor) {
    validateAuditorAndNotes(auditor, reason);
  }

  private String trimToNull(String value) {
    if (value == null) return null;
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private boolean equalsIgnoreCase(String a, String b) {
    if (a == null && b == null) return true;
    if (a == null || b == null) return false;
    return a.equalsIgnoreCase(b);
  }

  private String actorFrom(String value) {
    String trimmed = trimToNull(value);
    return trimmed == null ? "Sistema" : trimmed;
  }
}