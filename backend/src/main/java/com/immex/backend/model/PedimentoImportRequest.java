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

  @NotEmpty
  @Valid
  private List<Partida> partidas;

  public String getNumeroPedimento() { return numeroPedimento; }
  public void setNumeroPedimento(String numeroPedimento) { this.numeroPedimento = numeroPedimento; }
  public LocalDate getFecha() { return fecha; }
  public void setFecha(LocalDate fecha) { this.fecha = fecha; }
  public String getRazonSocial() { return razonSocial; }
  public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
  public List<Partida> getPartidas() { return partidas; }
  public void setPartidas(List<Partida> partidas) { this.partidas = partidas; }

  public static class Partida {
    @NotBlank
    private String material;

    @NotNull
    @Positive
    private Integer cantidad;

    @NotNull
    @Positive
    private BigDecimal valorUnitarioMxn;

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getValorUnitarioMxn() { return valorUnitarioMxn; }
    public void setValorUnitarioMxn(BigDecimal valorUnitarioMxn) { this.valorUnitarioMxn = valorUnitarioMxn; }
  }
}
