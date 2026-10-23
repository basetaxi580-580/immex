package com.immex.backend.service;

import com.immex.backend.model.PedimentoImportRequest;
import com.immex.backend.model.PedimentoImportResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class PedimentoImportService {
  private final MongoTemplate mongoTemplate;
  private final AuditEventService auditEventService;
  private final CloseMonthlyService closeMonthlyService;
  private final PlazoAduaneroService plazoAduaneroService;

  public PedimentoImportService(MongoTemplate mongoTemplate, AuditEventService auditEventService, CloseMonthlyService closeMonthlyService, PlazoAduaneroService plazoAduaneroService) {
    this.mongoTemplate = mongoTemplate;
    this.auditEventService = auditEventService;
    this.closeMonthlyService = closeMonthlyService;
    this.plazoAduaneroService = plazoAduaneroService;
  }

  public PedimentoImportResult importPedimento(PedimentoImportRequest request) {
    closeMonthlyService.requireSigned();
    ensureCollections();
    validateUniquePartidas(request.getPartidas());
    LocalDate fecha = request.getFecha();
    String pedimento = request.getNumeroPedimento().trim();
    String razonSocial = request.getRazonSocial().trim();
    String rfc = request.getRfc().trim();
    syncRazonSocial(razonSocial, rfc);
    BigDecimal total = BigDecimal.ZERO;
    int created = 0;

    for (PedimentoImportRequest.Partida partida : request.getPartidas()) {
      String material = partida.getMaterial().trim();
      Integer cantidad = partida.getCantidad();
      BigDecimal valorUnitario = partida.getValorUnitarioMxn();
      total = total.add(valorUnitario.multiply(BigDecimal.valueOf(cantidad)));

      String tipoMaterial = inferTipoMaterial(material);
      int plazoMeses = plazoAduaneroService.resolvePlazoMeses(tipoMaterial, partida.getRegimenAduanero());
      LocalDate fechaVencimiento = plazoMeses <= 0 ? null : fecha.plusMonths(plazoMeses);
      long diasRestantes = fechaVencimiento == null ? Long.MAX_VALUE : ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
      Document lote = new Document("material", material)
          .append("partida", partida.getPartida().trim())
          .append("unidad", partida.getUnidad().trim())
          .append("regimenAduanero", partida.getRegimenAduanero().trim())
          .append("tipoMaterial", tipoMaterial)
          .append("plazoMeses", plazoMeses)
          .append("saldoInicial", cantidad)
          .append("saldo", cantidad)
          .append("saldoFisico", cantidad)
          .append("saldoAduaneroPendiente", cantidad)
          .append("fechaEntrada", fecha.toString())
          .append("fechaVencimiento", fechaVencimiento == null ? null : fechaVencimiento.toString())
          .append("diasRestantes", diasRestantes)
          .append("estado", estadoDe(diasRestantes))
          .append("pedimento", pedimento)
          .append("razonSocial", razonSocial)
          .append("rfc", rfc)
          .append("valorUnitarioMxn", valorUnitario)
          .append("createdAt", Instant.now().toString())
          .append("updatedAt", Instant.now().toString())
          .append("movimientos", List.of(new Document("tipoMovimiento", "ENTRADA")
              .append("cantidad", cantidad)
              .append("saldoAntes", 0)
              .append("saldoDespues", cantidad)
              .append("saldoFisicoAntes", 0)
              .append("saldoFisicoDespues", cantidad)
              .append("saldoAduaneroAntes", 0)
              .append("saldoAduaneroDespues", cantidad)
              .append("fecha", fecha.toString())
              .append("usuario", "Sistema")
              .append("observacion", "Alta de pedimento")));
      mongoTemplate.insert(lote, "lote_material");
      created++;
    }

    mongoTemplate.insert(new Document("folio", pedimento)
        .append("fecha", fecha.toString())
        .append("razonSocial", razonSocial)
        .append("rfc", rfc)
        .append("partidas", created)
        .append("totalMxn", total)
        .append("createdAt", Instant.now().toString()), "pedimento");

    auditEventService.record("Pedimento", pedimento, "CREATE", "Sistema", "Alta de pedimento", razonSocial + " / partidas=" + created);
    return new PedimentoImportResult(pedimento, fecha, razonSocial, created, total);
  }

  private void validateUniquePartidas(List<PedimentoImportRequest.Partida> partidas) {
    Set<String> seen = new HashSet<>();
    for (PedimentoImportRequest.Partida partida : partidas) {
      String key = normalize(partida.getPartida()) + '|' + normalize(partida.getMaterial());
      if (!seen.add(key)) {
        throw new IllegalStateException("No se permiten partidas duplicadas en el mismo pedimento");
      }
    }
  }

  private void ensureCollections() {
    if (!mongoTemplate.collectionExists("pedimento")) {
      mongoTemplate.createCollection("pedimento");
    }
    if (!mongoTemplate.collectionExists("lote_material")) {
      mongoTemplate.createCollection("lote_material");
    }
    if (!mongoTemplate.collectionExists("razon_social")) {
      mongoTemplate.createCollection("razon_social");
    }
  }

  private void syncRazonSocial(String nombre, String rfc) {
    String normalizedNombre = nombre == null ? "" : nombre.trim();
    if (normalizedNombre.isEmpty()) {
      return;
    }
    String normalizedRfc = rfc == null ? "" : rfc.trim();
    var collection = mongoTemplate.getCollection("razon_social");
    Document existing = collection.find(new Document("nombre", normalizedNombre)).first();
    Document payload = new Document("nombre", normalizedNombre)
        .append("rfc", normalizedRfc)
        .append("programaImmex", existing != null ? String.valueOf(existing.getOrDefault("programaImmex", "IMMEX Operación")) : "IMMEX Operación")
        .append("activo", true);
    if (existing == null) {
      collection.insertOne(payload);
    } else {
      collection.updateOne(new Document("_id", existing.getObjectId("_id")), new Document("$set", payload));
    }
  }

  private String inferTipoMaterial(String material) {
    String normalized = material == null ? "" : material.toLowerCase();
    if (normalized.contains("resina") || normalized.contains("plástico") || normalized.contains("plastico")) {
      return "resina";
    }
    if (normalized.contains("conector") || normalized.contains("arnés") || normalized.contains("arnes")) {
      return "electronico";
    }
    if (normalized.contains("pigment") || normalized.contains("quim")) {
      return "quimico";
    }
    return "general";
  }

  private String estadoDe(long diasRestantes) {
    if (diasRestantes < 0) {
      return "vencido";
    }
    if (diasRestantes <= 30) {
      return "crítico";
    }
    if (diasRestantes <= 60) {
      return "urgente";
    }
    if (diasRestantes <= 90) {
      return "por vencer";
    }
    return "vigente";
  }

  private String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase();
  }
}