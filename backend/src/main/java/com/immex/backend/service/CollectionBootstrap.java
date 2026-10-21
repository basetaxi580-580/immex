package com.immex.backend.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class CollectionBootstrap implements ApplicationRunner {
  private static final int DEFAULT_LOTE_PLAZO_MESES = 18;
  private final MongoTemplate mongoTemplate;

  public CollectionBootstrap(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public void run(ApplicationArguments args) {
    ensureCollection("razon_social", List.of(
        new Document("nombre", "Aerosistemas del Norte, S.A. de C.V.")
            .append("rfc", "ASN9403159K2")
            .append("programaImmex", "IMMEX Manufactura")
            .append("activo", true),
        new Document("nombre", "Componentes Industriales del Bajío, S.A. de C.V.")
            .append("rfc", "CIB0209187Q4")
            .append("programaImmex", "IMMEX Servicios")
            .append("activo", true)));

    ensureCollection("pedimento", List.of(
        new Document("folio", "24 48 3021 9001234")
            .append("clave", "A1")
            .append("tipo", "Importación definitiva")
            .append("fecha", LocalDate.of(2026, 10, 2).toString())
            .append("razonSocial", "Aerosistemas del Norte, S.A. de C.V.")
            .append("fraccion", "39269099"),
        new Document("folio", "24 48 3021 9001278")
            .append("clave", "V1")
            .append("tipo", "Retorno virtual")
            .append("fecha", LocalDate.of(2026, 10, 11).toString())
            .append("razonSocial", "Componentes Industriales del Bajío, S.A. de C.V.")
            .append("fraccion", "85369099")));

    ensureCollection("lote_material", List.of(
        lote("Resina ABS natural", "resina", 1200, LocalDate.of(2026, 9, 18), "24 48 3021 9001234"),
        lote("Conector 12 pines", "electronico", 8000, LocalDate.of(2026, 5, 3), "24 48 3021 9001234")));
    refreshLoteMaterialState();

    ensureCollection("bom", List.of(
        new Document("numeroBom", "BOM-AR-2401")
            .append("producto", "Arnés eléctrico automotriz")
            .append("material", "Conector 12 pines")
            .append("cantidadRequerida", 4)
            .append("merma", 0.03),
        new Document("numeroBom", "BOM-ABS-2402")
            .append("producto", "Carcasa de control")
            .append("material", "Resina ABS natural")
            .append("cantidadRequerida", 1)
            .append("merma", 0.02)));

    ensureCollection("orden_produccion", List.of(
        new Document("numero", "OP-2026-1041")
            .append("producto", "Arnés eléctrico automotriz")
            .append("cantidadPlan", 600)
            .append("fechaInicio", LocalDate.of(2026, 10, 14).toString())
            .append("fechaFinEstimada", LocalDate.of(2026, 10, 22).toString())
            .append("estado", "En proceso"),
        new Document("numero", "OP-2026-1042")
            .append("producto", "Carcasa de control")
            .append("cantidadPlan", 420)
            .append("fechaInicio", LocalDate.of(2026, 10, 16).toString())
            .append("fechaFinEstimada", LocalDate.of(2026, 10, 24).toString())
            .append("estado", "Programada")));

    ensureCollection("descarga", List.of(
        new Document("documento", "DESC-77120")
            .append("pedimento", "24 48 3021 9001234")
            .append("material", "Resina ABS natural")
            .append("cantidad", 300)
            .append("fecha", LocalDate.of(2026, 10, 20).toString())
            .append("usuario", "Sistema")
            .append("observacion", "Descarga parcial registrada para cierre"),
        new Document("documento", "DESC-77121")
            .append("pedimento", "24 48 3021 9001278")
            .append("material", "Conector 12 pines")
            .append("cantidad", 1500)
            .append("fecha", LocalDate.of(2026, 10, 21).toString())
            .append("usuario", "Auditor IMMEX")
            .append("observacion", "Conciliación contra orden de producción")));

    ensureCollection("exportacion", List.of(
        new Document("folio", "EXP-2026-501")
            .append("cliente", "Northline Automotive LLC")
            .append("factura", "INV-2026-0891")
            .append("fecha", LocalDate.of(2026, 10, 19).toString())
            .append("valor", 128450.75)
            .append("pedimento", "24 48 3021 9001234"),
        new Document("folio", "EXP-2026-502")
            .append("cliente", "Midwest Controls Inc.")
            .append("factura", "INV-2026-0898")
            .append("fecha", LocalDate.of(2026, 10, 21).toString())
            .append("valor", 86420.10)
            .append("pedimento", "24 48 3021 9001278")));

    ensureCollection("evento_auditoria", List.of(
        new Document("entidad", "lote_material")
            .append("entidadId", "LM-001")
            .append("accion", "CREAR")
            .append("usuario", "Sistema")
            .append("motivo", "Alta inicial de material importado")
            .append("fecha", LocalDate.of(2026, 10, 20).toString()),
        new Document("entidad", "exportacion")
            .append("entidadId", "EXP-2026-501")
            .append("accion", "CERRAR")
            .append("usuario", "Auditor IMMEX")
            .append("motivo", "Cierre documental mensual")
            .append("fecha", LocalDate.of(2026, 10, 21).toString())));
  }

  private Document lote(String material, String tipoMaterial, int saldo, LocalDate fechaEntrada, String pedimento) {
    int plazoMeses = resolvePlazoMeses(tipoMaterial);
    LocalDate fechaVencimiento = fechaEntrada.plusMonths(plazoMeses);
    long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    return new Document("material", material)
        .append("tipoMaterial", tipoMaterial)
        .append("plazoMeses", plazoMeses)
        .append("saldo", saldo)
        .append("fechaEntrada", fechaEntrada.toString())
        .append("fechaVencimiento", fechaVencimiento.toString())
        .append("diasRestantes", diasRestantes)
        .append("estado", estadoDe(diasRestantes))
        .append("pedimento", pedimento);
  }

  private void refreshLoteMaterialState() {
    if (!mongoTemplate.collectionExists("lote_material")) {
      return;
    }
    mongoTemplate.getCollection("lote_material").find().forEach(doc -> {
      Object id = doc.get("_id");
      if (!(id instanceof ObjectId objectId)) {
        return;
      }
      LocalDate fechaEntrada = readDate(doc.get("fechaEntrada"));
      if (fechaEntrada == null) {
        return;
      }
      String tipoMaterial = stringValue(doc.get("tipoMaterial"));
      if (tipoMaterial.isBlank()) {
        tipoMaterial = inferTipoMaterial(stringValue(doc.get("material")));
      }
      int plazoMeses = doc.getInteger("plazoMeses", resolvePlazoMeses(tipoMaterial));
      LocalDate fechaVencimiento = fechaEntrada.plusMonths(plazoMeses);
      long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
      mongoTemplate.getCollection("lote_material").updateOne(
          new Document("_id", objectId),
          new Document("$set", new Document("tipoMaterial", tipoMaterial)
              .append("plazoMeses", plazoMeses)
              .append("fechaVencimiento", fechaVencimiento.toString())
              .append("diasRestantes", diasRestantes)
              .append("estado", estadoDe(diasRestantes))));
    });
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

  private LocalDate readDate(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof LocalDate localDate) {
      return localDate;
    }
    return LocalDate.parse(value.toString());
  }

  private String stringValue(Object value) {
    return value == null ? "" : value.toString();
  }

  private void ensureCollection(String collectionName, List<Document> samples) {
    if (!mongoTemplate.collectionExists(collectionName)) {
      mongoTemplate.createCollection(collectionName);
    }
    if (mongoTemplate.getCollection(collectionName).countDocuments() == 0) {
      mongoTemplate.insert(samples, collectionName);
    }
  }
}
