package com.immex.backend.service;

import com.immex.backend.model.DescargaRegistro;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoteMovimientoService {
  private final MongoTemplate mongoTemplate;

  public LoteMovimientoService(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public List<DescargaRegistro> consumirPeps(String material, int cantidad, String pedimento, String usuario, String observacion) {
    ensureCollections();
    List<Document> lotes = new ArrayList<>(mongoTemplate.getCollection("lote_material")
        .find(new Document("material", material))
        .into(new ArrayList<>()));
    lotes.sort(Comparator.comparing(this::fechaEntrada));

    int remaining = cantidad;
    List<DescargaRegistro> movimientos = new ArrayList<>();

    for (Document lote : lotes) {
      if (remaining <= 0) break;
      Object idValue = lote.get("_id");
      if (!(idValue instanceof ObjectId objectId)) continue;
      long saldo = saldoActual(lote);
      if (saldo <= 0) continue;
      long consume = Math.min(saldo, remaining);
      long saldoDespues = saldo - consume;
      updateSaldo(objectId, saldoDespues);
      movimientos.add(registrarDescarga(lote, objectId.toHexString(), pedimento, usuario, observacion, consume, saldo, saldoDespues));
      remaining -= (int) consume;
    }

    if (remaining > 0) {
      throw new IllegalStateException("No hay inventario suficiente para completar la descarga PEPS");
    }

    return movimientos;
  }

  private DescargaRegistro registrarDescarga(Document lote, String loteId, String pedimento, String usuario, String observacion, long cantidad, long saldoAntes, long saldoDespues) {
    DescargaRegistro registro = new DescargaRegistro();
    registro.setId(new ObjectId().toHexString());
    registro.setLoteId(loteId);
    registro.setPedimento(pedimento);
    registro.setMaterial(String.valueOf(lote.get("material")));
    registro.setCantidad((int) cantidad);
    registro.setValorUnitarioMxn(lote.get("valorUnitarioMxn") == null ? null : new java.math.BigDecimal(String.valueOf(lote.get("valorUnitarioMxn"))));
    registro.setFecha(LocalDate.now());
    registro.setTipoMovimiento("PEPS");
    registro.setSaldoAntes(saldoAntes);
    registro.setSaldoDespues(saldoDespues);
    registro.setUsuario(usuario);
    registro.setObservacion(observacion);

    mongoTemplate.insert(new Document("loteId", loteId)
        .append("pedimento", pedimento)
        .append("material", registro.getMaterial())
        .append("cantidad", cantidad)
        .append("fecha", registro.getFecha().toString())
        .append("tipoMovimiento", "PEPS")
        .append("saldoAntes", saldoAntes)
        .append("saldoDespues", saldoDespues)
        .append("usuario", usuario)
        .append("observacion", observacion)
        .append("createdAt", Instant.now().toString()), "descarga");

    return registro;
  }

  private void updateSaldo(ObjectId id, long saldoDespues) {
    long diasRestantes = 0;
    Document lote = mongoTemplate.getCollection("lote_material").find(new Document("_id", id)).first();
    if (lote != null) {
      LocalDate fechaVencimiento = fechaVencimiento(lote);
      diasRestantes = fechaVencimiento == null ? 0 : java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    }
    mongoTemplate.getCollection("lote_material").updateOne(
        new Document("_id", id),
        new Document("$set", new Document("saldo", saldoDespues)
            .append("diasRestantes", diasRestantes)
            .append("estado", saldoDespues <= 0 ? "vencido" : (diasRestantes <= 90 ? "por vencer" : "vigente"))
            .append("updatedAt", Instant.now().toString())));
  }

  private void ensureCollections() {
    if (!mongoTemplate.collectionExists("lote_material")) mongoTemplate.createCollection("lote_material");
    if (!mongoTemplate.collectionExists("descarga")) mongoTemplate.createCollection("descarga");
  }

  private long saldoActual(Document lote) {
    Number saldo = (Number) lote.get("saldo");
    return saldo == null ? 0L : saldo.longValue();
  }

  private LocalDate fechaEntrada(Document lote) {
    Object value = lote.get("fechaEntrada");
    return value == null ? LocalDate.MIN : LocalDate.parse(String.valueOf(value));
  }

  private LocalDate fechaVencimiento(Document lote) {
    Object value = lote.get("fechaVencimiento");
    return value == null ? null : LocalDate.parse(String.valueOf(value));
  }
}
