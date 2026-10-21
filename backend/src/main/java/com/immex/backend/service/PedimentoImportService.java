package com.immex.backend.service;

import com.immex.backend.model.PedimentoImportRequest;
import com.immex.backend.model.PedimentoImportResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class PedimentoImportService {
  private static final int DEFAULT_LOTE_PLAZO_MESES = 18;
  private final MongoTemplate mongoTemplate;
  private final AuditEventService auditEventService;

  public PedimentoImportService(MongoTemplate mongoTemplate, AuditEventService auditEventService) {
    this.mongoTemplate = mongoTemplate;
    this.auditEventService = auditEventService;
  }

  public PedimentoImportResult importPedimento(PedimentoImportRequest request) {
    ensureCollections();
    LocalDate fecha = request.getFecha();
    String pedimento = request.getNumeroPedimento().trim();
    String razonSocial = request.getRazonSocial().trim();
    BigDecimal total = BigDecimal.ZERO;
    int created = 0;

    for (PedimentoImportRequest.Partida partida : request.getPartidas()) {
      String material = partida.getMaterial().trim();
      Integer cantidad = partida.getCantidad();
      BigDecimal valorUnitario = partida.getValorUnitarioMxn();
      total = total.add(valorUnitario.multiply(BigDecimal.valueOf(cantidad)));

      String tipoMaterial = inferTipoMaterial(material);
      int plazoMeses = resolvePlazoMeses(tipoMaterial);
      LocalDate fechaVencimiento = fecha.plusMonths(plazoMeses);
      long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
      Document lote = new Document("material", material)
          .append("tipoMaterial", tipoMaterial)
          .append("plazoMeses", plazoMeses)
          .append("saldo", cantidad)
          .append("fechaEntrada", fecha.toString())
          .append("fechaVencimiento", fechaVencimiento.toString())
          .append("diasRestantes", diasRestantes)
          .append("estado", estadoDe(diasRestantes))
          .append("pedimento", pedimento)
          .append("razonSocial", razonSocial)
          .append("valorUnitarioMxn", valorUnitario)
          .append("createdAt", Instant.now().toString())
          .append("updatedAt", Instant.now().toString());
      mongoTemplate.insert(lote, "lote_material");
      created++;
    }

    mongoTemplate.insert(new Document("folio", pedimento)
        .append("fecha", fecha.toString())
        .append("razonSocial", razonSocial)
        .append("partidas", created)
        .append("totalMxn", total)
        .append("createdAt", Instant.now().toString()), "pedimento");

    auditEventService.record("Pedimento", pedimento, "CREATE", "Sistema", "Alta de pedimento", razonSocial + " / partidas=" + created);
    return new PedimentoImportResult(pedimento, fecha, razonSocial, created, total);
  }

  private void ensureCollections() {
    if (!mongoTemplate.collectionExists("pedimento")) {
      mongoTemplate.createCollection("pedimento");
    }
    if (!mongoTemplate.collectionExists("lote_material")) {
      mongoTemplate.createCollection("lote_material");
    }
  }

  private int resolvePlazoMeses(String tipoMaterial) {
    String normalized = tipoMaterial == null ? "" : tipoMaterial.trim().toLowerCase();
    return switch (normalized) {
      case "resina", "plastico", "plástico", "electronico", "electrónico", "metalmecanica", "metalmecánica", "quimico", "químico" -> DEFAULT_LOTE_PLAZO_MESES;
      default -> DEFAULT_LOTE_PLAZO_MESES;
    };
  }

  private String inferTipoMaterial(String material) {
    String normalized = material == null ? "" : material.toLowerCase();
    if (normalized.contains("resina") || normalized.contains("plástico") || normalized.contains("plastico")) {
      return "resina";
    }
    if (normalized.contains("conector") || normalized.contains("arnés") || normalized.contains("arnes")) {
      return "electronico";
    }
    return "general";
  }

  private String estadoDe(long diasRestantes) {
    if (diasRestantes < 0) {
      return "vencido";
    }
    if (diasRestantes <= 90) {
      return "por vencer";
    }
    return "vigente";
  }
}
