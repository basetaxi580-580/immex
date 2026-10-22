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
      String loteRazonSocial = normalize(stringValue(lote.get("razonSocial")));
      if (!razonSocial.isBlank() && !loteRazonSocial.equals(razonSocial)) continue;
      String material = stringValue(lote.get("material"));
      long saldo = numberValue(lote.get("saldo"));
      if (saldo <= 0) continue;
      LocalDate fechaVencimiento = readDate(lote.get("fechaVencimiento"));
      long diasRestantes = fechaVencimiento == null ? Long.MAX_VALUE : ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
      BigDecimal valorUnitario = moneyValue(lote.get("valorUnitarioMxn"));
      BigDecimal monto = valorUnitario.multiply(BigDecimal.valueOf(saldo));
      String estado = saldo <= 0 ? "cerrado" : (diasRestantes < 0 ? "vencido" : (diasRestantes <= 90 ? "por vencer" : "vigente"));
      rows.add(new DashboardMaterialRow(material, saldo, fechaVencimiento, monto, diasRestantes, estado));
      saldoTotal = saldoTotal.add(monto);
      if (diasRestantes < 0) {
        montoVencido = montoVencido.add(monto);
      } else if (diasRestantes <= 90) {
        montoPorVencer = montoPorVencer.add(monto);
      }
    }

    rows.sort(Comparator.comparing(DashboardMaterialRow::diasRestantes)
        .thenComparing(DashboardMaterialRow::montoEnRiesgo, Comparator.reverseOrder()));

    return new DashboardInventorySummary(saldoTotal, montoVencido, montoPorVencer, rows);
  }

  public LoteMaterialView materialView(String material, String razonSocial) {
    ensureCollections();
    String normalizedMaterial = normalize(material);
    String normalizedCompany = normalize(razonSocial);
    return mongoTemplate.getCollection("lote_material").find().into(new ArrayList<>()).stream()
        .filter(doc -> normalizedCompany.isBlank() || normalizedCompany.equals(normalize(stringValue(doc.get("razonSocial")))))
        .map(this::toView)
        .filter(view -> normalizedMaterial.isBlank() || normalize(view.material()).equals(normalizedMaterial) || view.material().toLowerCase().contains(normalizedMaterial))
        .sorted(Comparator.comparing(LoteMaterialView::diasRestantes).thenComparing(LoteMaterialView::fechaEntrada, Comparator.nullsLast(Comparator.naturalOrder())))
        .findFirst()
        .orElse(null);
  }

  public List<LoteMaterialView> materialViews(String material, String razonSocial) {
    ensureCollections();
    String normalizedMaterial = normalize(material);
    String normalizedCompany = normalize(razonSocial);
    return mongoTemplate.getCollection("lote_material").find().into(new ArrayList<>()).stream()
        .filter(doc -> normalizedCompany.isBlank() || normalizedCompany.equals(normalize(stringValue(doc.get("razonSocial")))))
        .map(this::toView)
        .filter(view -> normalizedMaterial.isBlank() || normalize(view.material()).equals(normalizedMaterial) || view.material().toLowerCase().contains(normalizedMaterial))
        .sorted(Comparator.comparing(LoteMaterialView::diasRestantes).thenComparing(LoteMaterialView::fechaEntrada, Comparator.nullsLast(Comparator.naturalOrder())))
        .toList();
  }

  private LoteMaterialView toView(Document lote) {
    LocalDate fechaEntrada = readDate(lote.get("fechaEntrada"));
    LocalDate fechaVencimiento = readDate(lote.get("fechaVencimiento"));
    long diasRestantes = fechaVencimiento == null ? Long.MAX_VALUE : ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    long saldo = numberValue(lote.get("saldo"));
    return new LoteMaterialView(
        stringValue(lote.get("_id")),
        stringValue(lote.get("material")),
        stringValue(lote.get("pedimento")),
        stringValue(lote.get("razonSocial")),
        saldo,
        fechaEntrada,
        fechaVencimiento,
        diasRestantes,
        estadoDe(saldo, diasRestantes),
        moneyValue(lote.get("valorUnitarioMxn")),
        stringValue(lote.get("tipoMaterial")));
  }

  private LocalDate readDateSafe(Document doc) {
    return readDate(doc.get("fechaEntrada"));
  }

  private void ensureCollections() {
    if (!mongoTemplate.collectionExists("lote_material")) mongoTemplate.createCollection("lote_material");
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
    if (diasRestantes <= 90) return "por vencer";
    return "vigente";
  }

  private String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase();
  }

  private String stringValue(Object value) {
    return value == null ? "" : value.toString();
  }
}
