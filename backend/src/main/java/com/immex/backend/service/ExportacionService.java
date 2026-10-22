package com.immex.backend.service;

import com.immex.backend.model.ExportacionRequest;
import com.immex.backend.model.ExportacionResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class ExportacionService {
  private final MongoTemplate mongoTemplate;
  private final LoteMovimientoService loteMovimientoService;
  private final AuditEventService auditEventService;
  private final CloseMonthlyService closeMonthlyService;

  public ExportacionService(MongoTemplate mongoTemplate, LoteMovimientoService loteMovimientoService, AuditEventService auditEventService, CloseMonthlyService closeMonthlyService) {
    this.mongoTemplate = mongoTemplate;
    this.loteMovimientoService = loteMovimientoService;
    this.auditEventService = auditEventService;
    this.closeMonthlyService = closeMonthlyService;
  }

  public ExportacionResult create(ExportacionRequest request) {
    closeMonthlyService.requireSigned();
    ensureCollections();
    BigDecimal totalDisponible = disponiblePorMaterial(request.getProducto());
    if (totalDisponible.compareTo(BigDecimal.valueOf(request.getCantidad())) < 0) {
      throw new IllegalStateException("No hay inventario suficiente para exportar");
    }

    List<Document> lotes = new ArrayList<>(mongoTemplate.getCollection("lote_material")
        .find(new Document("material", request.getProducto()))
        .into(new ArrayList<>()));
    lotes.sort((a, b) -> fechaEntrada(a).compareTo(fechaEntrada(b)));

    int remaining = request.getCantidad();
    List<String> lotesConsumidos = new ArrayList<>();
    BigDecimal totalExportado = BigDecimal.ZERO;

    for (Document lote : lotes) {
      if (remaining <= 0) break;
      Object idValue = lote.get("_id");
      if (!(idValue instanceof ObjectId objectId)) continue;
      long saldo = saldoActual(lote);
      if (saldo <= 0) continue;
      long consume = Math.min(saldo, remaining);
      loteMovimientoService.consumirPeps(
          request.getProducto(),
          (int) consume,
          request.getPedimentoExportacion(),
          auditor(request.getAuditor()),
          request.getObservacion());
      lotesConsumidos.add(objectId.toHexString());
      remaining -= (int) consume;
      totalExportado = totalExportado.add(valorUnitario(lote).multiply(BigDecimal.valueOf(consume)));
    }

    boolean completado = remaining == 0;
    mongoTemplate.insert(new Document("folio", request.getPedimentoExportacion())
        .append("fecha", request.getFecha().toString())
        .append("producto", request.getProducto())
        .append("cantidad", request.getCantidad())
        .append("totalExportado", totalExportado)
        .append("createdAt", Instant.now().toString()), "exportacion");
    auditEventService.record("Exportacion", request.getPedimentoExportacion(), "CREATE", auditor(request.getAuditor()), request.getObservacion(), "producto=" + request.getProducto() + ", cantidad=" + request.getCantidad() + ", completado=" + completado);
    return new ExportacionResult(request.getPedimentoExportacion(), request.getFecha(), request.getProducto(), request.getCantidad(), totalExportado, lotesConsumidos, completado, completado ? "Exportación registrada" : "Exportación parcial");
  }

  private void ensureCollections() {
    if (!mongoTemplate.collectionExists("exportacion")) mongoTemplate.createCollection("exportacion");
  }

  private BigDecimal disponiblePorMaterial(String material) {
    long saldo = mongoTemplate.getCollection("lote_material").find(new Document("material", material)).into(new ArrayList<>())
        .stream()
        .mapToLong(this::saldoActual)
        .sum();
    return BigDecimal.valueOf(saldo);
  }

  private long saldoActual(Document lote) {
    Number saldo = (Number) lote.get("saldo");
    return saldo == null ? 0L : saldo.longValue();
  }

  private BigDecimal valorUnitario(Document lote) {
    Object value = lote.get("valorUnitarioMxn");
    if (value == null) return BigDecimal.ONE;
    return new BigDecimal(String.valueOf(value));
  }

  private java.time.LocalDate fechaEntrada(Document lote) {
    Object value = lote.get("fechaEntrada");
    return value == null ? java.time.LocalDate.MIN : java.time.LocalDate.parse(String.valueOf(value));
  }

  private String auditor(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}