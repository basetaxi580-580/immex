package com.immex.backend.model;

import jakarta.validation.constraints.NotBlank;

public class CloseMonthlySignRequest {
  @NotBlank(message = "El usuario es obligatorio")
  private String actor;

  public String getActor() { return actor; }
  public void setActor(String actor) { this.actor = actor; }
}
