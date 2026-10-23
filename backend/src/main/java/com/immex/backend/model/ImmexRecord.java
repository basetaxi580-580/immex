package com.immex.backend.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("immex_records")
public class ImmexRecord {
  @Id
  private String id;
  private String pedimento;
  private String material;
  private String proveedor;
  private String movimiento;
  private String razonSocial;
  private String rfc;
  private String partida;
  private String unidad;
  private String regimenAduanero;
  private Integer cantidad;
  private BigDecimal valorUnitario;
  private LocalDate fechaIngreso;
  private LocalDate fechaVencimiento;
  private String status;
  private boolean activo;
  private String notas;
  private String auditor;
  private Instant createdAt;
  private Instant updatedAt;

  public ImmexRecord() {}

  public ImmexRecord(
      String id,
      String pedimento,
      String material,
      String proveedor,
      String movimiento,
      String razonSocial,
      String rfc,
      String partida,
      String unidad,
      String regimenAduanero,
      Integer cantidad,
      BigDecimal valorUnitario,
      LocalDate fechaIngreso,
      LocalDate fechaVencimiento,
      String status,
      boolean activo,
      String notas,
      String auditor,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.pedimento = pedimento;
    this.material = material;
    this.proveedor = proveedor;
    this.movimiento = movimiento;
    this.razonSocial = razonSocial;
    this.rfc = rfc;
    this.partida = partida;
    this.unidad = unidad;
    this.regimenAduanero = regimenAduanero;
    this.cantidad = cantidad;
    this.valorUnitario = valorUnitario;
    this.fechaIngreso = fechaIngreso;
    this.fechaVencimiento = fechaVencimiento;
    this.status = status;
    this.activo = activo;
    this.notas = notas;
    this.auditor = auditor;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
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
  public BigDecimal getValorUnitario() { return valorUnitario; }
  public void setValorUnitario(BigDecimal valorUnitario) { this.valorUnitario = valorUnitario; }
  public LocalDate getFechaIngreso() { return fechaIngreso; }
  public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }
  public LocalDate getFechaVencimiento() { return fechaVencimiento; }
  public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public boolean isActivo() { return activo; }
  public void setActivo(boolean activo) { this.activo = activo; }
  public String getNotas() { return notas; }
  public void setNotas(String notas) { this.notas = notas; }
  public String getAuditor() { return auditor; }
  public void setAuditor(String auditor) { this.auditor = auditor; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}