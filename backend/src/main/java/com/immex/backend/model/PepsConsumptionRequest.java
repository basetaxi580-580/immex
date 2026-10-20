package com.immex.backend.model;

import java.time.Instant;

public class PepsConsumptionRequest {
  private String material;
  private Integer quantity;
  private String reason;
  private String auditor;

  public String getMaterial() { return material; }
  public void setMaterial(String material) { this.material = material; }
  public Integer getQuantity() { return quantity; }
  public void setQuantity(Integer quantity) { this.quantity = quantity; }
  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }
  public String getAuditor() { return auditor; }
  public void setAuditor(String auditor) { this.auditor = auditor; }
}
