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

  public ImmexRecordService(ImmexRecordRepository repository, AuditEventService auditEventService) {
    this.repository = repository;
    this.auditEventService = auditEventService;
  }

  public List<ImmexRecord> findAll() {
    return repository.findAll();
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
        "Pedimento " + saved.getPedimento() + " / " + saved.getMaterial());
    return saved;
  }

  public ImmexRecord update(String id, ImmexRecordRequest request) {
    ImmexRecord existing = findById(id);
    existing.setPedimento(request.getPedimento().trim());
    existing.setMaterial(request.getMaterial().trim());
    existing.setProveedor(request.getProveedor().trim());
    existing.setMovimiento(request.getMovimiento().trim());
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
        "Pedimento " + saved.getPedimento() + " / " + saved.getMaterial());
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
        "Pedimento " + existing.getPedimento() + " / " + existing.getMaterial());
  }

  public ImmexDashboardSummary summary() {
    List<ImmexRecord> records = repository.findAll();
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

  public ImmexCloseReport buildCloseReport() {
    List<ImmexRecord> records = repository.findAll();
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

  public PepsOperationResult consumePeps(PepsConsumptionRequest request) {
    List<ImmexRecord> candidates = repository.findAll().stream()
        .filter(record -> record.isActivo())
        .filter(record -> record.getMaterial() != null && record.getMaterial().equalsIgnoreCase(request.getMaterial().trim()))
        .sorted(Comparator.comparing(ImmexRecord::getFechaIngreso, Comparator.nullsLast(Comparator.naturalOrder())))
        .toList();

    if (candidates.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay inventario disponible para PEPS");
    }

    int remaining = request.getQuantity();
    int processed = 0;
    for (ImmexRecord record : candidates) {
      if (remaining <= 0) break;
      int available = record.getCantidad() == null ? 0 : record.getCantidad();
      if (available <= 0) continue;
      int consume = Math.min(available, remaining);
      record.setCantidad(available - consume);
      record.setUpdatedAt(Instant.now());
      if (record.getCantidad() == 0) {
        record.setStatus("Cerrado");
        record.setActivo(false);
      } else {
        record.setStatus("Conciliación");
      }
      repository.save(record);
      processed += consume;
      remaining -= consume;
    }

    boolean completed = remaining == 0;
    String message = completed
        ? "Consumo PEPS aplicado correctamente"
        : "Consumo parcial: no hubo inventario suficiente para completar la solicitud";
    auditEventService.record(
        "ImmexRecord",
        request.getMaterial(),
        "PEPS_CONSUME",
        actorFrom(request.getAuditor()),
        request.getReason(),
        "Solicitado=" + request.getQuantity() + ", procesado=" + processed + ", completado=" + completed);
    return new PepsOperationResult(request.getMaterial(), request.getQuantity(), processed, completed, message, Instant.now());
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
