package com.immex.backend.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class ProduccionMensualRequest {
  @NotBlank
  private String producto;

  @NotNull
  @Positive
  private Integer cantidadProducida;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = true)
  private BigDecimal mermaPct;

  private String auditor;
  private String observacion;

  public String getProducto() { return producto; }
  public void setProducto(String producto) { this.producto = producto; }
  public Integer getCantidadProducida() { return cantidadProducida; }
  public void setCantidadProducida(Integer cantidadProducida) { this.cantidadProducida = cantidadProducida; }
  public BigDecimal getMermaPct() { return mermaPct; }
  public void setMermaPct(BigDecimal mermaPct) { this.mermaPct = mermaPct; }
  public String getAuditor() { return auditor; }
  public void setAuditor(String auditor) { this.auditor = auditor; }
  public String getObservacion() { return observacion; }
  public void setObservacion(String observacion) { this.observacion = observacion; }
}
