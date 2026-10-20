package com.immex.backend.model;

import java.time.Instant;

public class PepsOperationResult {
  private final String material;
  private final int requestedQuantity;
  private final int processedQuantity;
  private final boolean completed;
  private final String message;
  private final Instant processedAt;

  public PepsOperationResult(String material, int requestedQuantity, int processedQuantity, boolean completed, String message, Instant processedAt) {
    this.material = material;
    this.requestedQuantity = requestedQuantity;
    this.processedQuantity = processedQuantity;
    this.completed = completed;
    this.message = message;
    this.processedAt = processedAt;
  }

  public String getMaterial() { return material; }
  public int getRequestedQuantity() { return requestedQuantity; }
  public int getProcessedQuantity() { return processedQuantity; }
  public boolean isCompleted() { return completed; }
  public String getMessage() { return message; }
  public Instant getProcessedAt() { return processedAt; }
}
