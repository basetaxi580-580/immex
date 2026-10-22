package com.immex.backend.model;

import jakarta.validation.constraints.NotBlank;

public class ConciliacionResolveRequest {
  @NotBlank(message = "El responsable es obligatorio")
  private String responsable;

  @NotBlank(message = "La resolución es obligatoria")
  private String resolucion;

  public String getResponsable() { return responsable; }
  public void setResponsable(String responsable) { this.responsable = responsable; }
  public String getResolucion() { return resolucion; }
  public void setResolucion(String resolucion) { this.resolucion = resolucion; }
}