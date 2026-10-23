package com.immex.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class PlazoAduaneroService {
  private final MongoTemplate mongoTemplate;

  public PlazoAduaneroService(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public int resolvePlazoMeses(String material, String regimenAduanero) {
    ensureCollection();
    String normalizedMaterial = normalize(material);
    String normalizedRegimen = normalize(regimenAduanero);
    List<Document> reglas = new ArrayList<>(mongoTemplate.getCollection("plazo_aduanero")
        .find()
        .into(new ArrayList<>()));
    reglas.sort(Comparator.comparingInt(rule -> -specificity(rule, normalizedMaterial, normalizedRegimen)));

    for (Document regla : reglas) {
      if (!matches(regla.getString("tipoMaterial"), normalizedMaterial)) continue;
      if (!matches(regla.getString("regimenAduanero"), normalizedRegimen)) continue;
      Integer meses = regla.getInteger("meses");
      if (meses != null && meses > 0) {
        return meses;
      }
    }

    if (normalizedRegimen.contains("definitiv")) {
      return 0;
    }
    if (normalizedRegimen.contains("temporal")) {
      if (normalizedMaterial.contains("quim")) return 6;
      if (normalizedMaterial.contains("plást") || normalizedMaterial.contains("plast")) return 12;
      if (normalizedMaterial.contains("resina") || normalizedMaterial.contains("conector") || normalizedMaterial.contains("arnes") || normalizedMaterial.contains("arnés") || normalizedMaterial.contains("electron")) return 18;
      return 12;
    }
    if (normalizedMaterial.contains("quim")) return 6;
    if (normalizedMaterial.contains("plást") || normalizedMaterial.contains("plast")) return 12;
    if (normalizedMaterial.contains("resina") || normalizedMaterial.contains("conector") || normalizedMaterial.contains("arnes") || normalizedMaterial.contains("arnés") || normalizedMaterial.contains("electron")) return 18;
    return 12;
  }

  private int specificity(Document rule, String material, String regimen) {
    int score = 0;
    String ruleMaterial = normalize(rule.getString("tipoMaterial"));
    String ruleRegimen = normalize(rule.getString("regimenAduanero"));
    if (!ruleMaterial.isBlank() && ruleMaterial.equals(material)) score += 2;
    if (!ruleRegimen.isBlank() && ruleRegimen.equals(regimen)) score += 2;
    if (!ruleMaterial.isBlank() && material.contains(ruleMaterial)) score += 1;
    if (!ruleRegimen.isBlank() && regimen.contains(ruleRegimen)) score += 1;
    return score;
  }

  private boolean matches(String expected, String actual) {
    if (expected == null || expected.isBlank()) return true;
    String normalizedExpected = normalize(expected);
    if (normalizedExpected.equals("*")) return true;
    return actual.contains(normalizedExpected) || normalizedExpected.contains(actual) || actual.equals(normalizedExpected);
  }

  private String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase();
  }

  private void ensureCollection() {
    if (!mongoTemplate.collectionExists("plazo_aduanero")) {
      mongoTemplate.createCollection("plazo_aduanero");
    }
  }
}