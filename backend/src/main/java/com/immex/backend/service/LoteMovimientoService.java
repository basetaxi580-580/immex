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

  public List<DescargaRegistro> simularPeps(String material, int cantidad, String pedimento, String usuario, String observacion) {
    return planPeps(material, cantidad, pedimento, usuario, observacion, false);
  }

  public List<DescargaRegistro> consumirPeps(String material, int cantidad, String pedimento, String usuario, String observacion) {
    return planPeps(material, cantidad, pedimento, usuario, observacion, true);
  }

  public List<DescargaRegistro> revertirPeps(String material, int cantidad, String pedimento, String usuario, String observacion) {
    ensureCollections();
    List<Document> applied = new ArrayList<>(mongoTemplate.getCollection("descarga")
        .find(new Document("material", material).append("tipoMovimiento", "PEPS"))
        .sort(new Document("createdAt", -1))
        .into(new ArrayList<>()));

    int remaining = cantidad;
    List<DescargaRegistro> reversions = new ArrayList<>();

    for (Document movimiento : applied) {
      if (remaining <= 0) break;
      Object loteIdValue = movimiento.get("loteId");
      if (loteIdValue == null) continue;
      String loteId = String.valueOf(loteIdValue);
      Object idValue = findObjectIdByIdString(loteId);
      if (!(idValue instanceof ObjectId objectId)) continue;

      long saldoAntes = longValue(movimiento.get("saldoAntes"));
      long saldoDespues = longValue(movimiento.get("saldoDespues"));
      long saldoFisicoAntes = longValue(movimiento.get("saldoFisicoAntes"));
      long saldoFisicoDespues = longValue(movimiento.get("saldoFisicoDespues"));
      long saldoAduaneroAntes = longValue(movimiento.get("saldoAduaneroAntes"));
      long saldoAduaneroDespues = longValue(movimiento.get("saldoAduaneroDespues"));
      long cantidadMovimiento = longValue(movimiento.get("cantidad"));
      long revertir = Math.min(cantidadMovimiento, remaining);
      long nuevoSaldo = saldoDespues + revertir;
      if (revertir <= 0) continue;
      updateSaldo(objectId, nuevoSaldo, saldoFisicoDespues + revertir, saldoAduaneroDespues + revertir);
      reversions.add(registrarReversion(loteId, pedimento, usuario, observacion, revertir, saldoAntes, nuevoSaldo, saldoFisicoAntes, saldoFisicoDespues + revertir, saldoAduaneroAntes, saldoAduaneroDespues + revertir));
      remaining -= (int) revertir;

      if (revertir < cantidadMovimiento) {
        break;
      }
    }

    if (remaining > 0) {
      throw new IllegalStateException("No hay movimientos PEPS suficientes para revertir la cantidad solicitada");
    }

    return reversions;
  }

  private List<DescargaRegistro> planPeps(String material, int cantidad, String pedimento, String usuario, String observacion, boolean persistir) {
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
      long saldoFisico = numberValue(lote.get("saldoFisico"));
      long saldoAduanero = numberValue(lote.get("saldoAduaneroPendiente"));
      long saldoFisicoDespues = Math.max(0, saldoFisico - consume);
      long saldoAduaneroDespues = Math.max(0, saldoAduanero - consume);
      if (persistir) {
        updateSaldo(objectId, saldoDespues, saldoFisicoDespues, saldoAduaneroDespues);
      }
      movimientos.add(registrarDescarga(lote, objectId.toHexString(), pedimento, usuario, observacion, consume, saldo, saldoDespues, saldoFisico, saldoFisicoDespues, saldoAduanero, saldoAduaneroDespues, persistir));
      remaining -= (int) consume;
    }

    if (remaining > 0) {
      throw new IllegalStateException("No hay inventario suficiente para completar la descarga PEPS");
    }

    return movimientos;
  }

  private DescargaRegistro registrarDescarga(Document lote, String loteId, String pedimento, String usuario, String observacion, long cantidad, long saldoAntes, long saldoDespues, long saldoFisicoAntes, long saldoFisicoDespues, long saldoAduaneroAntes, long saldoAduaneroDespues, boolean persistir) {
    DescargaRegistro registro = new DescargaRegistro();
    registro.setId(new ObjectId().toHexString());
    registro.setLoteId(loteId);
    registro.setPedimento(pedimento);
    registro.setMaterial(String.valueOf(lote.get("material")));
    registro.setRfc(String.valueOf(lote.getOrDefault("rfc", "")));
    registro.setPartida(String.valueOf(lote.getOrDefault("partida", "")));
    registro.setUnidad(String.valueOf(lote.getOrDefault("unidad", "")));
    registro.setRegimenAduanero(String.valueOf(lote.getOrDefault("regimenAduanero", "")));
    registro.setCantidad((int) cantidad);
    registro.setValorUnitarioMxn(lote.get("valorUnitarioMxn") == null ? null : new java.math.BigDecimal(String.valueOf(lote.get("valorUnitarioMxn"))));
    registro.setFecha(LocalDate.now());
    registro.setTipoMovimiento(persistir ? "PEPS" : "SIMULACION");
    registro.setSaldoAntes(saldoAntes);
    registro.setSaldoDespues(saldoDespues);
    registro.setSaldoFisicoAntes(saldoFisicoAntes);
    registro.setSaldoFisicoDespues(saldoFisicoDespues);
    registro.setSaldoAduaneroAntes(saldoAduaneroAntes);
    registro.setSaldoAduaneroDespues(saldoAduaneroDespues);
    registro.setUsuario(usuario);
    registro.setObservacion(observacion);

    if (persistir) {
      mongoTemplate.insert(new Document("loteId", loteId)
          .append("pedimento", pedimento)
          .append("material", registro.getMaterial())
          .append("rfc", registro.getRfc())
          .append("partida", registro.getPartida())
          .append("unidad", registro.getUnidad())
          .append("regimenAduanero", registro.getRegimenAduanero())
          .append("cantidad", cantidad)
          .append("fecha", registro.getFecha().toString())
          .append("tipoMovimiento", "PEPS")
          .append("saldoAntes", saldoAntes)
          .append("saldoDespues", saldoDespues)
          .append("saldoFisicoAntes", saldoFisicoAntes)
          .append("saldoFisicoDespues", saldoFisicoDespues)
          .append("saldoAduaneroAntes", saldoAduaneroAntes)
          .append("saldoAduaneroDespues", saldoAduaneroDespues)
          .append("usuario", usuario)
          .append("observacion", observacion)
          .append("createdAt", Instant.now().toString()), "descarga");
    }

    return registro;
  }

  private DescargaRegistro registrarReversion(String loteId, String pedimento, String usuario, String observacion, long cantidad, long saldoAntes, long saldoDespues, long saldoFisicoAntes, long saldoFisicoDespues, long saldoAduaneroAntes, long saldoAduaneroDespues) {
    DescargaRegistro registro = new DescargaRegistro();
    registro.setId(new ObjectId().toHexString());
    registro.setLoteId(loteId);
    registro.setPedimento(pedimento);
    registro.setMaterial(findMaterialByLoteId(loteId));
    registro.setCantidad((int) cantidad);
    registro.setFecha(LocalDate.now());
    registro.setTipoMovimiento("PEPS_REVERT");
    registro.setSaldoAntes(saldoAntes);
    registro.setSaldoDespues(saldoDespues);
    registro.setSaldoFisicoAntes(saldoFisicoAntes);
    registro.setSaldoFisicoDespues(saldoFisicoDespues);
    registro.setSaldoAduaneroAntes(saldoAduaneroAntes);
    registro.setSaldoAduaneroDespues(saldoAduaneroDespues);
    registro.setUsuario(usuario);
    registro.setObservacion(observacion);

    mongoTemplate.insert(new Document("loteId", loteId)
        .append("pedimento", pedimento)
        .append("material", registro.getMaterial())
        .append("cantidad", cantidad)
        .append("fecha", registro.getFecha().toString())
        .append("tipoMovimiento", "PEPS_REVERT")
        .append("saldoAntes", saldoAntes)
        .append("saldoDespues", saldoDespues)
        .append("saldoFisicoAntes", saldoFisicoAntes)
        .append("saldoFisicoDespues", saldoFisicoDespues)
        .append("saldoAduaneroAntes", saldoAduaneroAntes)
        .append("saldoAduaneroDespues", saldoAduaneroDespues)
        .append("usuario", usuario)
        .append("observacion", observacion)
        .append("createdAt", Instant.now().toString()), "descarga");

    return registro;
  }

  private void updateSaldo(ObjectId id, long saldoDespues, long saldoFisicoDespues, long saldoAduaneroDespues) {
    long diasRestantes = 0;
    Document lote = mongoTemplate.getCollection("lote_material").find(new Document("_id", id)).first();
    if (lote != null) {
      LocalDate fechaVencimiento = fechaVencimiento(lote);
      diasRestantes = fechaVencimiento == null ? 0 : java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    }
    mongoTemplate.getCollection("lote_material").updateOne(
        new Document("_id", id),
        new Document("$set", new Document("saldo", Math.max(0, saldoDespues))
            .append("saldoFisico", Math.max(0, saldoFisicoDespues))
            .append("saldoAduaneroPendiente", Math.max(0, saldoAduaneroDespues))
            .append("diasRestantes", diasRestantes)
            .append("estado", saldoDespues <= 0 ? "cerrado" : (diasRestantes <= 90 ? "por vencer" : "vigente"))
            .append("updatedAt", Instant.now().toString())));
  }

  private Object findObjectIdByIdString(String id) {
    try {
      return new ObjectId(id);
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

  private String findMaterialByLoteId(String loteId) {
    try {
      Document lote = mongoTemplate.getCollection("lote_material").find(new Document("_id", new ObjectId(loteId))).first();
      return lote == null ? null : String.valueOf(lote.get("material"));
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

  private long longValue(Object value) {
    if (value instanceof Number number) {
      return number.longValue();
    }
    if (value == null) {
      return 0L;
    }
    try {
      return Long.parseLong(String.valueOf(value));
    } catch (NumberFormatException ex) {
      return 0L;
    }
  }

  private long numberValue(Object value) {
    return longValue(value);
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