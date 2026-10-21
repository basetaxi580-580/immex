package com.immex.backend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class ExportacionRequest {
  @NotBlank
  private String pedimentoExportacion;

  @NotNull
  private LocalDate fecha;

  @NotBlank
  private String producto;

  @NotNull
  @Positive
  private Integer cantidad;

  private String auditor;
  private String observacion;

  public String getPedimentoExportacion() { return pedimentoExportacion; }
  public void setPedimentoExportacion(String pedimentoExportacion) { this.pedimentoExportacion = pedimentoExportacion; }
  public LocalDate getFecha() { return fecha; }
  public void setFecha(LocalDate fecha) { this.fecha = fecha; }
  public String getProducto() { return producto; }
  public void setProducto(String producto) { this.producto = producto; }
  public Integer getCantidad() { return cantidad; }
  public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
  public String getAuditor() { return auditor; }
  public void setAuditor(String auditor) { this.auditor = auditor; }
  public String getObservacion() { return observacion; }
  public void setObservacion(String observacion) { this.observacion = observacion; }
}
