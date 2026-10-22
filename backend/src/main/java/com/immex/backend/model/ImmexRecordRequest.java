package com.immex.backend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ImmexRecordRequest {
  @NotBlank
  private String pedimento;

  @NotBlank
  private String material;

  @NotBlank
  private String proveedor;

  @NotBlank
  private String movimiento;

  private String razonSocial;

  @NotNull
  @Positive
  private Integer cantidad;

  @NotNull
  @Positive
  private BigDecimal valorUnitario;

  @NotNull
  private LocalDate fechaIngreso;

  @NotNull
  private LocalDate fechaVencimiento;

  @NotBlank
  private String status;

  @NotNull
  private Boolean activo;

  private String notas;
  private String auditor;

  public String getPedimento() { return pedimento; }
  public void setPedimento(String pedimento) { this.pedimento = pedimento; }
  public String getMaterial() { return material; }
  public void setMaterial(String material) { this.material = material; }
  public String getProveedor() { return proveedor; }
  public void setProveedor(String proveedor) { this.proveedor = proveedor; }
  public String getMovimiento() { return movimiento; }
  public void setMovimiento(String movimiento) { this.movimiento = movimiento; }
  public String getRazonSocial() { return razonSocial; }
  public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
  public Integer getCantidad() { return cantidad; }
  public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
  public BigDecimal getValorUnitario() { return valorUnitario; }
  public void setValorUnitario(BigDecimal valorUnitario) { this.valorUnitario = valorUnitario; }
  public LocalDate getFechaIngreso() { return fechaIngreso; }
  public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }
  public LocalDate getFechaVencimiento() { return fechaVencimiento; }
  public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Boolean getActivo() { return activo; }
  public void setActivo(Boolean activo) { this.activo = activo; }
  public String getNotas() { return notas; }
  public void setNotas(String notas) { this.notas = notas; }
  public String getAuditor() { return auditor; }
  public void setAuditor(String auditor) { this.auditor = auditor; }
}
