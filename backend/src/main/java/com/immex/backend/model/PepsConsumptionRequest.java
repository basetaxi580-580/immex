package com.immex.backend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;

public class PepsConsumptionRequest {
  @NotBlank(message = "El material es obligatorio")
  private String material;

  @NotNull
  @Positive(message = "La cantidad debe ser mayor a cero")
  private Integer quantity;

  @NotBlank(message = "El motivo es obligatorio")
  private String reason;

  @NotBlank(message = "El usuario es obligatorio")
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
