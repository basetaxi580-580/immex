package com.immex.backend.service;

import com.immex.backend.model.DashboardInventorySummary;
import com.immex.backend.model.DashboardMaterialRow;
import com.immex.backend.model.LoteMaterialView;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventoryDashboardService {
  private final MongoTemplate mongoTemplate;

  public InventoryDashboardService(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public DashboardInventorySummary inventory(String razonSocialFilter) {
    ensureCollections();
    String razonSocial = normalize(razonSocialFilter);
    List<Document> lotes = mongoTemplate.getCollection("lote_material").find().into(new ArrayList<>());
    List<DashboardMaterialRow> rows = new ArrayList<>();
    BigDecimal saldoTotal = BigDecimal.ZERO;
    BigDecimal montoVencido = BigDecimal.ZERO;
    BigDecimal montoPorVencer = BigDecimal.ZERO;

    for (Document lote : lotes) {
      if (isClosedLike(lote)) continue;
      String loteRazonSocial = normalize(stringValue(lote.get("razonSocial")));
      if (!razonSocial.isBlank() && !loteRazonSocial.equals(razonSocial)) continue;
      String material = stringValue(lote.get("material"));
      String pedimento = stringValue(lote.get("pedimento"));
      String rfc = stringValue(lote.get("rfc"));
      String partida = stringValue(lote.get("partida"));
      String unidad = stringValue(lote.get("unidad"));
      String regimenAduanero = stringValue(lote.get("regimenAduanero"));
      long saldo = numberValue(lote.get("saldo"));
      long saldoFisico = numberValue(lote.getOrDefault("saldoFisico", saldo));
      long saldoAduaneroPendiente = numberValue(lote.getOrDefault("saldoAduaneroPendiente", saldo));
      if (saldo <= 0 && saldoFisico <= 0 && saldoAduaneroPendiente <= 0) continue;
      LocalDate fechaVencimiento = readDate(lote.get("fechaVencimiento"));
      long diasRestantes = fechaVencimiento == null ? Long.MAX_VALUE : ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
      BigDecimal valorUnitario = moneyValue(lote.get("valorUnitarioMxn"));
      BigDecimal monto = valorUnitario.multiply(BigDecimal.valueOf(Math.max(saldo, saldoAduaneroPendiente)));
      String estado = diasRestantes < 0 ? "vencido" : (diasRestantes <= 30 ? "vencido" : diasRestantes <= 60 ? "próximo a vencer" : diasRestantes <= 90 ? "por vencer" : "vigente");
      rows.add(new DashboardMaterialRow(material, pedimento, rfc, partida, unidad, regimenAduanero, saldo, saldoFisico, saldoAduaneroPendiente, fechaVencimiento, monto, diasRestantes, estado));
      saldoTotal = saldoTotal.add(monto);
      if (diasRestantes < 0) {
        montoVencido = montoVencido.add(monto);
      } else if (diasRestantes <= 90) {
        montoPorVencer = montoPorVencer.add(monto);
      }
    }

    rows.sort(Comparator.comparing((DashboardMaterialRow row) -> urgencyRank(row.diasRestantes()))
        .thenComparing(DashboardMaterialRow::diasRestantes)
        .thenComparing(DashboardMaterialRow::montoEnRiesgo, Comparator.reverseOrder()));

    return new DashboardInventorySummary(saldoTotal, montoVencido, montoPorVencer, rows);
  }

  public LoteMaterialView materialView(String material, String razonSocial) {
    ensureCollections();
    String normalizedMaterial = normalize(material);
    String normalizedCompany = normalize(razonSocial);
    return mongoTemplate.getCollection("lote_material").find().into(new ArrayList<>()).stream()
        .filter(doc -> !isClosedLike(doc))
        .filter(doc -> normalizedCompany.isBlank() || normalizedCompany.equals(normalize(stringValue(doc.get("razonSocial")))))
        .map(this::toView)
        .filter(view -> normalizedMaterial.isBlank() || normalize(view.material()).equals(normalizedMaterial) || view.material().toLowerCase().contains(normalizedMaterial))
        .sorted(Comparator.comparing((LoteMaterialView row) -> urgencyRank(row.diasRestantes()))
            .thenComparing(LoteMaterialView::diasRestantes)
            .thenComparing(LoteMaterialView::fechaEntrada, Comparator.nullsLast(Comparator.naturalOrder())))
        .findFirst()
        .orElse(null);
  }

  public List<LoteMaterialView> materialViews(String material, String razonSocial) {
    ensureCollections();
    String normalizedMaterial = normalize(material);
    String normalizedCompany = normalize(razonSocial);
    return mongoTemplate.getCollection("lote_material").find().into(new ArrayList<>()).stream()
        .filter(doc -> !isClosedLike(doc))
        .filter(doc -> normalizedCompany.isBlank() || normalizedCompany.equals(normalize(stringValue(doc.get("razonSocial")))))
        .map(this::toView)
        .filter(view -> normalizedMaterial.isBlank() || normalize(view.material()).equals(normalizedMaterial) || view.material().toLowerCase().contains(normalizedMaterial))
        .sorted(Comparator.comparing((LoteMaterialView row) -> urgencyRank(row.diasRestantes()))
            .thenComparing(LoteMaterialView::diasRestantes)
            .thenComparing(LoteMaterialView::fechaEntrada, Comparator.nullsLast(Comparator.naturalOrder())))
        .toList();
  }

  private LoteMaterialView toView(Document lote) {
    LocalDate fechaEntrada = readDate(lote.get("fechaEntrada"));
    LocalDate fechaVencimiento = readDate(lote.get("fechaVencimiento"));
    long diasRestantes = fechaVencimiento == null ? Long.MAX_VALUE : ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    long saldo = numberValue(lote.get("saldo"));
    long saldoFisico = numberValue(lote.getOrDefault("saldoFisico", saldo));
    long saldoAduaneroPendiente = numberValue(lote.getOrDefault("saldoAduaneroPendiente", saldo));
    String estado = normalize(stringValue(lote.get("estado")));
    if (estado.isBlank()) {
      estado = estadoDe(saldo, diasRestantes);
    }
    return new LoteMaterialView(
        stringValue(lote.get("_id")),
        stringValue(lote.get("material")),
        stringValue(lote.get("pedimento")),
        stringValue(lote.get("razonSocial")),
        stringValue(lote.get("rfc")),
        stringValue(lote.get("partida")),
        stringValue(lote.get("unidad")),
        stringValue(lote.get("regimenAduanero")),
        saldo,
        saldoFisico,
        saldoAduaneroPendiente,
        fechaEntrada,
        fechaVencimiento,
        diasRestantes,
        estado,
        moneyValue(lote.get("valorUnitarioMxn")),
        stringValue(lote.get("tipoMaterial")));
  }

  private void ensureCollections() {
    if (!mongoTemplate.collectionExists("lote_material")) mongoTemplate.createCollection("lote_material");
  }

  private int urgencyRank(long diasRestantes) {
    if (diasRestantes < 0) return 0;
    if (diasRestantes <= 30) return 1;
    if (diasRestantes <= 60) return 2;
    if (diasRestantes <= 90) return 3;
    return 4;
  }

  private long numberValue(Object value) {
    if (value == null) return 0L;
    if (value instanceof Number number) return number.longValue();
    try { return Long.parseLong(value.toString()); } catch (NumberFormatException ex) { return 0L; }
  }

  private BigDecimal moneyValue(Object value) {
    if (value == null) return BigDecimal.ZERO;
    if (value instanceof BigDecimal decimal) return decimal;
    if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
    try { return new BigDecimal(value.toString()); } catch (NumberFormatException ex) { return BigDecimal.ZERO; }
  }

  private LocalDate readDate(Object value) {
    if (value == null) return null;
    if (value instanceof LocalDate localDate) return localDate;
    try { return LocalDate.parse(value.toString()); } catch (Exception ex) { return null; }
  }

  private String estadoDe(long saldo, long diasRestantes) {
    if (saldo <= 0) return "cerrado";
    if (diasRestantes < 0) return "vencido";
    if (diasRestantes <= 30) return "vencido";
    if (diasRestantes <= 60) return "próximo a vencer";
    if (diasRestantes <= 90) return "por vencer";
    return "vigente";
  }

  private boolean isClosedLike(Document doc) {
    String estado = normalize(stringValue(doc.get("estado")));
    String movimiento = normalize(stringValue(doc.get("movimiento")));
    String tipoMovimiento = normalize(stringValue(doc.get("tipoMovimiento")));
    return "cerrado".equals(estado)
        || "cerrado".equals(movimiento)
        || "exportacion".equals(movimiento)
        || "exportación".equals(movimiento)
        || "exportacion".equals(tipoMovimiento)
        || "exportación".equals(tipoMovimiento);
  }

  private String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase();
  }

  private String stringValue(Object value) {
    return value == null ? "" : value.toString();
  }
}