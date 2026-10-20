package com.immex.backend.model;

public class ImmexDashboardSummary {
  private final long totalRecords;
  private final long riskTotal;
  private final long expiringSoonCount;
  private final long alert90Count;
  private final long alert60Count;
  private final long alert30Count;
  private final long entryCount;
  private final long consumptionCount;
  private final long exportCount;
  private final long closeReadyCount;
  private final ImmexRecord pepsCandidate;

  public ImmexDashboardSummary(
      long totalRecords,
      long riskTotal,
      long expiringSoonCount,
      long alert90Count,
      long alert60Count,
      long alert30Count,
      long entryCount,
      long consumptionCount,
      long exportCount,
      long closeReadyCount,
      ImmexRecord pepsCandidate) {
    this.totalRecords = totalRecords;
    this.riskTotal = riskTotal;
    this.expiringSoonCount = expiringSoonCount;
    this.alert90Count = alert90Count;
    this.alert60Count = alert60Count;
    this.alert30Count = alert30Count;
    this.entryCount = entryCount;
    this.consumptionCount = consumptionCount;
    this.exportCount = exportCount;
    this.closeReadyCount = closeReadyCount;
    this.pepsCandidate = pepsCandidate;
  }

  public long getTotalRecords() { return totalRecords; }
  public long getRiskTotal() { return riskTotal; }
  public long getExpiringSoonCount() { return expiringSoonCount; }
  public long getAlert90Count() { return alert90Count; }
  public long getAlert60Count() { return alert60Count; }
  public long getAlert30Count() { return alert30Count; }
  public long getEntryCount() { return entryCount; }
  public long getConsumptionCount() { return consumptionCount; }
  public long getExportCount() { return exportCount; }
  public long getCloseReadyCount() { return closeReadyCount; }
  public ImmexRecord getPepsCandidate() { return pepsCandidate; }
}
