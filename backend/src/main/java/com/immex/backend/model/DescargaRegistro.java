package com.immex.backend.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DescargaRegistro {
  private String id;
  private String loteId;
  private String pedimento;
  private String material;
  private Integer cantidad;
  private BigDecimal valorUnitarioMxn;
  private LocalDate fecha;
  private String tipoMovimiento;
  private Long saldoAntes;
  private Long saldoDespues;
  private String usuario;
  private String observacion;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getLoteId() { return loteId; }
  public void setLoteId(String loteId) { this.loteId = loteId; }
  public String getPedimento() { return pedimento; }
  public void setPedimento(String pedimento) { this.pedimento = pedimento; }
  public String getMaterial() { return material; }
  public void setMaterial(String material) { this.material = material; }
  public Integer getCantidad() { return cantidad; }
  public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
  public BigDecimal getValorUnitarioMxn() { return valorUnitarioMxn; }
  public void setValorUnitarioMxn(BigDecimal valorUnitarioMxn) { this.valorUnitarioMxn = valorUnitarioMxn; }
  public LocalDate getFecha() { return fecha; }
  public void setFecha(LocalDate fecha) { this.fecha = fecha; }
  public String getTipoMovimiento() { return tipoMovimiento; }
  public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
  public Long getSaldoAntes() { return saldoAntes; }
  public void setSaldoAntes(Long saldoAntes) { this.saldoAntes = saldoAntes; }
  public Long getSaldoDespues() { return saldoDespues; }
  public void setSaldoDespues(Long saldoDespues) { this.saldoDespues = saldoDespues; }
  public String getUsuario() { return usuario; }
  public void setUsuario(String usuario) { this.usuario = usuario; }
  public String getObservacion() { return observacion; }
  public void setObservacion(String observacion) { this.observacion = observacion; }
}
