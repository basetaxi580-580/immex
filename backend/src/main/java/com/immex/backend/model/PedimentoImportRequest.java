package com.immex.backend.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PedimentoImportRequest {
  @NotBlank
  private String numeroPedimento;

  @NotNull
  private LocalDate fecha;

  @NotBlank
  private String razonSocial;

  @NotBlank
  private String rfc;

  @NotEmpty
  @Valid
  private List<Partida> partidas;

  public String getNumeroPedimento() { return numeroPedimento; }
  public void setNumeroPedimento(String numeroPedimento) { this.numeroPedimento = numeroPedimento; }
  public LocalDate getFecha() { return fecha; }
  public void setFecha(LocalDate fecha) { this.fecha = fecha; }
  public String getRazonSocial() { return razonSocial; }
  public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
  public String getRfc() { return rfc; }
  public void setRfc(String rfc) { this.rfc = rfc; }
  public List<Partida> getPartidas() { return partidas; }
  public void setPartidas(List<Partida> partidas) { this.partidas = partidas; }

  public static class Partida {
    @NotBlank
    private String partida;

    @NotBlank
    private String material;

    @NotBlank
    private String unidad;

    @NotBlank
    private String regimenAduanero;

    @NotNull
    @Positive
    private Integer cantidad;

    @NotNull
    @Positive
    private BigDecimal valorUnitarioMxn;

    public String getPartida() { return partida; }
    public void setPartida(String partida) { this.partida = partida; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
    public String getRegimenAduanero() { return regimenAduanero; }
    public void setRegimenAduanero(String regimenAduanero) { this.regimenAduanero = regimenAduanero; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getValorUnitarioMxn() { return valorUnitarioMxn; }
    public void setValorUnitarioMxn(BigDecimal valorUnitarioMxn) { this.valorUnitarioMxn = valorUnitarioMxn; }
  }
}