package com.immex.backend.model;

import jakarta.validation.constraints.NotBlank;

public class CloseMonthlyStepUpdateRequest {
  @NotBlank(message = "La etapa es obligatoria")
  private String stepKey;
  @NotBlank(message = "El usuario es obligatorio")
  private String actor;
  @NotBlank(message = "El motivo es obligatorio")
  private String reason;
  private String detail;

  public String getStepKey() { return stepKey; }
  public void setStepKey(String stepKey) { this.stepKey = stepKey; }
  public String getActor() { return actor; }
  public void setActor(String actor) { this.actor = actor; }
  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }
  public String getDetail() { return detail; }
  public void setDetail(String detail) { this.detail = detail; }
}
