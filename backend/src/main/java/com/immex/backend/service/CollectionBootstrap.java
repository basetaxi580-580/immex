package com.immex.backend.service;

import java.time.LocalDate;
import java.util.List;
import org.bson.Document;
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

    ensureCollection("lote_material", List.of(
        new Document("material", "Resina ABS natural")
            .append("saldo", 1200)
            .append("fechaEntrada", LocalDate.of(2026, 9, 18).toString())
            .append("fechaVencimiento", LocalDate.of(2027, 3, 18).toString())
            .append("pedimento", "24 48 3021 9001234"),
        new Document("material", "Conector 12 pines")
            .append("saldo", 8000)
            .append("fechaEntrada", LocalDate.of(2026, 5, 3).toString())
            .append("fechaVencimiento", LocalDate.of(2026, 10, 30).toString())
            .append("pedimento", "24 48 3021 9001234")));

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

  private void ensureCollection(String collectionName, List<Document> samples) {
    if (!mongoTemplate.collectionExists(collectionName)) {
      mongoTemplate.createCollection(collectionName);
    }
    if (mongoTemplate.getCollection(collectionName).countDocuments() == 0) {
      mongoTemplate.insert(samples, collectionName);
    }
  }
}
