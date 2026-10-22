package com.immex.backend.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class ProduccionMensualRequest {
  @NotBlank(message = "El producto es obligatorio")
  private String producto;

  @NotNull
  @Positive(message = "La cantidad producida debe ser mayor a cero")
  private Integer cantidadProducida;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = true, message = "La merma no puede ser negativa")
  private BigDecimal mermaPct;

  @NotBlank(message = "El usuario es obligatorio")
  private String auditor;

  @NotBlank(message = "El motivo es obligatorio")
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
