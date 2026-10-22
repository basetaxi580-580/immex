package com.immex.backend.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DescargaRegistro {
  private String id;
  private String loteId;
  private String pedimento;
  private String material;
  private String rfc;
  private String partida;
  private String unidad;
  private String regimenAduanero;
  private Integer cantidad;
  private BigDecimal valorUnitarioMxn;
  private LocalDate fecha;
  private String tipoMovimiento;
  private Long saldoAntes;
  private Long saldoDespues;
  private Long saldoFisicoAntes;
  private Long saldoFisicoDespues;
  private Long saldoAduaneroAntes;
  private Long saldoAduaneroDespues;
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
  public String getRfc() { return rfc; }
  public void setRfc(String rfc) { this.rfc = rfc; }
  public String getPartida() { return partida; }
  public void setPartida(String partida) { this.partida = partida; }
  public String getUnidad() { return unidad; }
  public void setUnidad(String unidad) { this.unidad = unidad; }
  public String getRegimenAduanero() { return regimenAduanero; }
  public void setRegimenAduanero(String regimenAduanero) { this.regimenAduanero = regimenAduanero; }
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
  public Long getSaldoFisicoAntes() { return saldoFisicoAntes; }
  public void setSaldoFisicoAntes(Long saldoFisicoAntes) { this.saldoFisicoAntes = saldoFisicoAntes; }
  public Long getSaldoFisicoDespues() { return saldoFisicoDespues; }
  public void setSaldoFisicoDespues(Long saldoFisicoDespues) { this.saldoFisicoDespues = saldoFisicoDespues; }
  public Long getSaldoAduaneroAntes() { return saldoAduaneroAntes; }
  public void setSaldoAduaneroAntes(Long saldoAduaneroAntes) { this.saldoAduaneroAntes = saldoAduaneroAntes; }
  public Long getSaldoAduaneroDespues() { return saldoAduaneroDespues; }
  public void setSaldoAduaneroDespues(Long saldoAduaneroDespues) { this.saldoAduaneroDespues = saldoAduaneroDespues; }
  public String getUsuario() { return usuario; }
  public void setUsuario(String usuario) { this.usuario = usuario; }
  public String getObservacion() { return observacion; }
  public void setObservacion(String observacion) { this.observacion = observacion; }
}