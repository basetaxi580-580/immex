package com.immex.backend.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class ImmexCloseReport {
  private final Instant generatedAt;
  private final long totalRecords;
  private final long openRecords;
  private final long expiringSoonCount;
  private final BigDecimal saldoTotalMxn;
  private final BigDecimal montoEnRiesgoMxn;
  private final ImmexRecord pepsCandidate;
  private final List<String> warnings;
  private final List<ClosePendingItem> pendingItems;
  private final List<String> steps;
  private final List<String> history;
  private final String estado;

  public ImmexCloseReport(
      Instant generatedAt,
      long totalRecords,
      long openRecords,
      long expiringSoonCount,
      BigDecimal saldoTotalMxn,
      BigDecimal montoEnRiesgoMxn,
      ImmexRecord pepsCandidate,
      List<String> warnings,
      List<ClosePendingItem> pendingItems,
      String estado) {
    this.generatedAt = generatedAt;
    this.totalRecords = totalRecords;
    this.openRecords = openRecords;
    this.expiringSoonCount = expiringSoonCount;
    this.saldoTotalMxn = saldoTotalMxn;
    this.montoEnRiesgoMxn = montoEnRiesgoMxn;
    this.pepsCandidate = pepsCandidate;
    this.warnings = warnings;
    this.pendingItems = pendingItems;
    this.steps = List.of();
    this.history = List.of();
    this.estado = estado;
  }

  public Instant getGeneratedAt() { return generatedAt; }
  public long getTotalRecords() { return totalRecords; }
  public long getOpenRecords() { return openRecords; }
  public long getExpiringSoonCount() { return expiringSoonCount; }
  public BigDecimal getSaldoTotalMxn() { return saldoTotalMxn; }
  public BigDecimal getMontoEnRiesgoMxn() { return montoEnRiesgoMxn; }
  public ImmexRecord getPepsCandidate() { return pepsCandidate; }
  public List<String> getWarnings() { return warnings; }
  public List<ClosePendingItem> getPendingItems() { return pendingItems; }
  public List<String> getSteps() { return steps; }
  public List<String> getHistory() { return history; }
  public String getEstado() { return estado; }
}
