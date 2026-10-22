package com.immex.backend.service;

import java.math.BigDecimal;
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

    ensureCollection("plazo_aduanero", List.of(
        new Document("tipoMaterial", "resina").append("regimenAduanero", "immex temporal").append("meses", 18),
        new Document("tipoMaterial", "electronico").append("regimenAduanero", "immex temporal").append("meses", 12),
        new Document("tipoMaterial", "quimico").append("regimenAduanero", "immex temporal").append("meses", 6),
        new Document("tipoMaterial", "plástico").append("regimenAduanero", "immex temporal").append("meses", 12),
        new Document("tipoMaterial", "plastico").append("regimenAduanero", "immex temporal").append("meses", 12),
        new Document("tipoMaterial", "*").append("regimenAduanero", "*").append("meses", 12)));

    ensureCollection("lote_material", List.of(
        lote("Resina ABS natural", "001", "kg", "IMMEX temporal", "resina", 1200, LocalDate.of(2026, 8, 20), "24 48 3021 9001234", "Aerosistemas del Norte, S.A. de C.V.", "ASN9403159K2", new BigDecimal("3.20")),
        lote("Regranulado ABS reciclado", "002", "kg", "IMMEX temporal", "resina", 180, LocalDate.of(2025, 7, 15), "24 48 3021 9001234", "Aerosistemas del Norte, S.A. de C.V.", "ASN9403159K2", new BigDecimal("2.10")),
        lote("Pigmento masterbatch negro", "003", "kg", "IMMEX temporal", "quimico", 40, LocalDate.of(2024, 12, 1), "24 48 3021 9001234", "Aerosistemas del Norte, S.A. de C.V.", "ASN9403159K2", new BigDecimal("95.00")),
        lote("Conector 12 pines", "A-01", "pieza", "IMMEX temporal", "electronico", 12000, LocalDate.of(2026, 8, 1), "24 48 3021 9001278", "Componentes Industriales del Bajío, S.A. de C.V.", "CIB0209187Q4", new BigDecimal("3.20")),
        lote("Terminal hembra 6.3 mm", "A-02", "pieza", "IMMEX temporal", "electronico", 25000, LocalDate.of(2025, 7, 20), "24 48 3021 9001278", "Componentes Industriales del Bajío, S.A. de C.V.", "CIB0209187Q4", new BigDecimal("0.48")),
        lote("Cinta PVC automotriz", "A-03", "rollo", "IMMEX temporal", "plástico", 9000, LocalDate.of(2024, 11, 15), "24 48 3021 9001278", "Componentes Industriales del Bajío, S.A. de C.V.", "CIB0209187Q4", new BigDecimal("0.12"))));
    refreshLoteMaterialState();

    ensureCollection("bom", List.of(
        new Document("numeroBom", "BOM-AR-2601")
            .append("producto", "Arnés eléctrico automotriz")
            .append("material", "Conector 12 pines")
            .append("cantidadRequerida", 4)
            .append("merma", 0.03),
        new Document("numeroBom", "BOM-AR-2601")
            .append("producto", "Arnés eléctrico automotriz")
            .append("material", "Terminal hembra 6.3 mm")
            .append("cantidadRequerida", 18)
            .append("merma", 0.04),
        new Document("numeroBom", "BOM-AR-2601")
            .append("producto", "Arnés eléctrico automotriz")
            .append("material", "Cinta PVC automotriz")
            .append("cantidadRequerida", 6)
            .append("merma", 0.02),
        new Document("numeroBom", "BOM-AR-2601")
            .append("producto", "Arnés eléctrico automotriz")
            .append("material", "Manguito termoencogible")
            .append("cantidadRequerida", 4)
            .append("merma", 0.01),
        new Document("numeroBom", "BOM-AR-2601")
            .append("producto", "Arnés eléctrico automotriz")
            .append("material", "Etiqueta de trazabilidad")
            .append("cantidadRequerida", 1)
            .append("merma", 0.0),
        new Document("numeroBom", "BOM-AR-2601")
            .append("producto", "Arnés eléctrico automotriz")
            .append("material", "Terminal de tierra")
            .append("cantidadRequerida", 2)
            .append("merma", 0.02),
        new Document("numeroBom", "BOM-AR-2601")
            .append("producto", "Arnés eléctrico automotriz")
            .append("material", "Grommet de protección")
            .append("cantidadRequerida", 1)
            .append("merma", 0.01),
        new Document("numeroBom", "BOM-AR-2601")
            .append("producto", "Arnés eléctrico automotriz")
            .append("material", "Cinta textil")
            .append("cantidadRequerida", 3)
            .append("merma", 0.03),
        new Document("numeroBom", "BOM-AR-2601")
            .append("producto", "Arnés eléctrico automotriz")
            .append("material", "Conduit corrugado")
            .append("cantidadRequerida", 2)
            .append("merma", 0.05),
        new Document("numeroBom", "BOM-ABS-2601")
            .append("producto", "Carcasa de control")
            .append("material", "Resina ABS natural")
            .append("cantidadRequerida", 1)
            .append("merma", 0.04),
        new Document("numeroBom", "BOM-ABS-2601")
            .append("producto", "Carcasa de control")
            .append("material", "Regranulado ABS reciclado")
            .append("cantidadRequerida", 0.2)
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

  private Document lote(String material, String partida, String unidad, String regimenAduanero, String tipoMaterial, int saldo, LocalDate fechaEntrada, String pedimento, String razonSocial, String rfc, BigDecimal valorUnitarioMxn) {
    int plazoMeses = resolvePlazoMeses(tipoMaterial, regimenAduanero);
    LocalDate fechaVencimiento = fechaEntrada.plusMonths(plazoMeses);
    long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    return new Document("material", material)
        .append("partida", partida)
        .append("unidad", unidad)
        .append("regimenAduanero", regimenAduanero)
        .append("tipoMaterial", tipoMaterial)
        .append("plazoMeses", plazoMeses)
        .append("saldoInicial", saldo)
        .append("saldo", saldo)
        .append("saldoFisico", saldo)
        .append("saldoAduaneroPendiente", saldo)
        .append("fechaEntrada", fechaEntrada.toString())
        .append("fechaVencimiento", fechaVencimiento.toString())
        .append("diasRestantes", diasRestantes)
        .append("estado", estadoDe(diasRestantes))
        .append("pedimento", pedimento)
        .append("razonSocial", razonSocial)
        .append("rfc", rfc)
        .append("valorUnitarioMxn", valorUnitarioMxn);
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
      String regimenAduanero = stringValue(doc.get("regimenAduanero"));
      int plazoMeses = doc.getInteger("plazoMeses", resolvePlazoMeses(tipoMaterial, regimenAduanero));
      LocalDate fechaVencimiento = fechaEntrada.plusMonths(plazoMeses);
      long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
      BigDecimal valorUnitarioMxn = moneyValue(doc.get("valorUnitarioMxn")).compareTo(BigDecimal.ZERO) > 0
          ? moneyValue(doc.get("valorUnitarioMxn"))
          : defaultValorUnitario(stringValue(doc.get("material")));
      mongoTemplate.getCollection("lote_material").updateOne(
          new Document("_id", objectId),
          new Document("$set", new Document("tipoMaterial", tipoMaterial)
              .append("plazoMeses", plazoMeses)
              .append("fechaVencimiento", fechaVencimiento.toString())
              .append("diasRestantes", diasRestantes)
              .append("estado", estadoDe(diasRestantes))
              .append("valorUnitarioMxn", valorUnitarioMxn)));
    });
  }

  private int resolvePlazoMeses(String tipoMaterial, String regimenAduanero) {
    String material = tipoMaterial == null ? "" : tipoMaterial.trim().toLowerCase();
    String regimen = regimenAduanero == null ? "" : regimenAduanero.trim().toLowerCase();
    if (regimen.contains("definitiv")) {
      return 0;
    }
    if (regimen.contains("temporal")) {
      if (material.contains("quim")) return 6;
      if (material.contains("plást") || material.contains("plast")) return 12;
      if (material.contains("resina") || material.contains("conector") || material.contains("arnes") || material.contains("arnés") || material.contains("electron")) return 18;
      return 12;
    }
    if (material.contains("quim")) return 6;
    if (material.contains("plást") || material.contains("plast")) return 12;
    if (material.contains("resina") || material.contains("conector") || material.contains("arnes") || material.contains("arnés") || material.contains("electron")) return 18;
    return 12;
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

  private long numberValue(Object value) {
    if (value == null) {
      return 0L;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    try {
      return Long.parseLong(value.toString());
    } catch (NumberFormatException ex) {
      return 0L;
    }
  }

  private BigDecimal moneyValue(Object value) {
    if (value == null) {
      return BigDecimal.ZERO;
    }
    if (value instanceof BigDecimal decimal) {
      return decimal;
    }
    if (value instanceof Number number) {
      return BigDecimal.valueOf(number.doubleValue());
    }
    try {
      return new BigDecimal(value.toString());
    } catch (NumberFormatException ex) {
      return BigDecimal.ZERO;
    }
  }

  private BigDecimal defaultValorUnitario(String material) {
    String normalized = material == null ? "" : material.toLowerCase();
    if (normalized.contains("conector")) {
      return new BigDecimal("1.85");
    }
    if (normalized.contains("resina")) {
      return new BigDecimal("3.20");
    }
    return new BigDecimal("1.00");
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