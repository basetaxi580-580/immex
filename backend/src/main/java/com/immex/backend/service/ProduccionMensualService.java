package com.immex.backend.service;

import com.immex.backend.model.ProduccionMensualLine;
import com.immex.backend.model.ProduccionMensualRequest;
import com.immex.backend.model.ProduccionMensualResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProduccionMensualService {
  private final MongoTemplate mongoTemplate;
  private final LoteMovimientoService loteMovimientoService;
  private final AuditEventService auditEventService;

  public ProduccionMensualService(MongoTemplate mongoTemplate, LoteMovimientoService loteMovimientoService, AuditEventService auditEventService) {
    this.mongoTemplate = mongoTemplate;
    this.loteMovimientoService = loteMovimientoService;
    this.auditEventService = auditEventService;
  }

  public ProduccionMensualResult preview(ProduccionMensualRequest request) {
    return calcular(request, false);
  }

  public ProduccionMensualResult ejecutar(ProduccionMensualRequest request) {
    return calcular(request, true);
  }

  private ProduccionMensualResult calcular(ProduccionMensualRequest request, boolean ejecutar) {
    ensureCollections();
    Document bom = buscarBom(request.getProducto());
    if (bom == null) {
      throw new IllegalStateException("No existe BOM para el producto seleccionado");
    }
    List<Document> componentes = componentesParaProducto(request.getProducto());
    if (componentes.isEmpty()) {
      throw new IllegalStateException("El BOM no tiene componentes configurados");
    }

    BigDecimal mermaFactor = request.getMermaPct().divide(BigDecimal.valueOf(100));
    List<ProduccionMensualLine> resumen = new ArrayList<>();
    int movimientos = 0;

    for (Document componente : componentes) {
      String material = String.valueOf(componente.get("material"));
      BigDecimal porUnidad = new BigDecimal(String.valueOf(componente.getOrDefault("cantidadRequerida", 1)));
      BigDecimal requeridoBase = porUnidad.multiply(BigDecimal.valueOf(request.getCantidadProducida()));
      BigDecimal requeridoConMerma = requeridoBase.multiply(BigDecimal.ONE.add(mermaFactor));
      int requeridoEntero = requeridoConMerma.setScale(0, RoundingMode.CEILING).intValueExact();

      int descargado;
      List<String> lotesAfectados = new ArrayList<>();
      if (ejecutar) {
        var lotes = loteMovimientoService.consumirPeps(
            material,
            requeridoEntero,
            String.valueOf(bom.getOrDefault("numeroBom", request.getProducto())),
            auditor(request.getAuditor()),
            request.getObservacion());
        descargado = lotes.stream().mapToInt(l -> l.getCantidad() == null ? 0 : l.getCantidad()).sum();
        movimientos += lotes.size();
        lotesAfectados = lotes.stream().map(com.immex.backend.model.DescargaRegistro::getLoteId).toList();
      } else {
        descargado = saldoDisponible(material, requeridoEntero);
      }

      BigDecimal saldoPendiente = requeridoConMerma.subtract(BigDecimal.valueOf(descargado)).max(BigDecimal.ZERO);
      resumen.add(new ProduccionMensualLine(material, requeridoConMerma, BigDecimal.valueOf(descargado), saldoPendiente, lotesAfectados));
    }

    boolean completado = resumen.stream().allMatch(linea -> linea.saldoPendiente().compareTo(BigDecimal.ZERO) == 0);
    String message = ejecutar
        ? (completado ? "Producción mensual validada y descargada por PEPS" : "Producción mensual procesada parcialmente")
        : (completado ? "Resumen listo para confirmar" : "Resumen parcial; faltaría inventario si confirma");
    if (ejecutar) {
      auditEventService.record("ProduccionMensual", request.getProducto(), "PRODUCCION_MENSUAL", auditor(request.getAuditor()), request.getObservacion(), "partidas=" + resumen.size() + ", movimientos=" + movimientos + ", completado=" + completado);
    }
    return new ProduccionMensualResult(request.getProducto(), request.getCantidadProducida(), request.getMermaPct(), resumen, movimientos, completado, message);
  }

  private int saldoDisponible(String material, int requeridoEntero) {
    return mongoTemplate.getCollection("lote_material").find(new Document("material", material)).into(new ArrayList<>())
        .stream()
        .mapToInt(doc -> ((Number) doc.getOrDefault("saldo", 0)).intValue())
        .sum() >= requeridoEntero ? requeridoEntero : (int) mongoTemplate.getCollection("lote_material").find(new Document("material", material)).into(new ArrayList<>())
        .stream().mapToLong(doc -> ((Number) doc.getOrDefault("saldo", 0)).longValue()).sum();
  }

  private String auditor(String value) {
    return value == null || value.isBlank() ? "Sistema" : value.trim();
  }

  private Document buscarBom(String producto) {
    return mongoTemplate.getCollection("bom").find(new Document("producto", producto)).first();
  }

  private List<Document> componentesParaProducto(String producto) {
    return mongoTemplate.getCollection("bom").find(new Document("producto", producto)).into(new ArrayList<>());
  }

  private void ensureCollections() {
    if (!mongoTemplate.collectionExists("bom")) mongoTemplate.createCollection("bom");
  }
}
