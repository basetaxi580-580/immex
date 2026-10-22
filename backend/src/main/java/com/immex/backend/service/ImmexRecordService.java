package com.immex.backend.service;

import com.immex.backend.model.AuditEvent;
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

  public ImmexRecordService(ImmexRecordRepository repository, AuditEventService auditEventService, LoteMovimientoService loteMovimientoService) {
    this.repository = repository;
    this.auditEventService = auditEventService;
    this.loteMovimientoService = loteMovimientoService;
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
    Instant now = Instant.now();
    ImmexRecord record = new ImmexRecord(
        UUID.randomUUID().toString(),
        request.getPedimento().trim(),
        request.getMaterial().trim(),
        request.getProveedor().trim(),
        request.getMovimiento().trim(),
        trimToNull(request.getRazonSocial()),
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
    ImmexRecord existing = findById(id);
    existing.setPedimento(request.getPedimento().trim());
    existing.setMaterial(request.getMaterial().trim());
    existing.setProveedor(request.getProveedor().trim());
    existing.setMovimiento(request.getMovimiento().trim());
    existing.setRazonSocial(trimToNull(request.getRazonSocial()));
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
    long riskTotal = records.stream().map(this::lineRisk).mapToLong(BigDecimal::longValue).sum();
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

    return new ImmexCloseReport(Instant.now(), records.size(), openRecords, expiringSoonCount, riskTotal, pepsCandidate, warnings);
  }

  public PepsOperationResult simulatePeps(PepsConsumptionRequest request) {
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
    return new PepsOperationResult(request.getMaterial(), request.getQuantity(), processed, completed, message, Instant.now(), movimientos);
  }

  public PepsOperationResult applyPeps(PepsConsumptionRequest request) {
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
    auditEventService.record(
        "Descarga",
        request.getMaterial(),
        "PEPS_REVERT",
        actorFrom(request.getAuditor()),
        request.getReason(),
        "Simulación descartada sin cambios en inventario");
    int quantity = request.getQuantity() == null ? 0 : request.getQuantity();
    return new PepsOperationResult(request.getMaterial(), quantity, 0, true, "Simulación revertida; no se guardaron cambios", Instant.now(), java.util.List.of());
  }

  public List<AuditEvent> listAuditEvents() {
    return auditEventService.findAll();
  }

  private BigDecimal lineRisk(ImmexRecord record) {
    if (record.getCantidad() == null || record.getValorUnitario() == null) {
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

  private boolean matchesCompany(ImmexRecord record, String razonSocial) {
    if (razonSocial == null || razonSocial.isBlank() || "all".equalsIgnoreCase(razonSocial)) {
      return true;
    }
    String recordCompany = trimToNull(record.getRazonSocial());
    return recordCompany != null && recordCompany.equalsIgnoreCase(razonSocial.trim());
  }

  private String trimToNull(String value) {
    if (value == null) return null;
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String actorFrom(String auditor) {
    String value = trimToNull(auditor);
    return value == null ? "Sistema" : value;
  }
}
