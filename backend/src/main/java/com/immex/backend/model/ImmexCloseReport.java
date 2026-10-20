package com.immex.backend.model;

import java.time.Instant;
import java.util.List;

public class ImmexCloseReport {
  private final Instant generatedAt;
  private final long totalRecords;
  private final long openRecords;
  private final long expiringSoonCount;
  private final long riskTotal;
  private final ImmexRecord pepsCandidate;
  private final List<String> warnings;

  public ImmexCloseReport(
      Instant generatedAt,
      long totalRecords,
      long openRecords,
      long expiringSoonCount,
      long riskTotal,
      ImmexRecord pepsCandidate,
      List<String> warnings) {
    this.generatedAt = generatedAt;
    this.totalRecords = totalRecords;
    this.openRecords = openRecords;
    this.expiringSoonCount = expiringSoonCount;
    this.riskTotal = riskTotal;
    this.pepsCandidate = pepsCandidate;
    this.warnings = warnings;
  }

  public Instant getGeneratedAt() { return generatedAt; }
  public long getTotalRecords() { return totalRecords; }
  public long getOpenRecords() { return openRecords; }
  public long getExpiringSoonCount() { return expiringSoonCount; }
  public long getRiskTotal() { return riskTotal; }
  public ImmexRecord getPepsCandidate() { return pepsCandidate; }
  public List<String> getWarnings() { return warnings; }
}
