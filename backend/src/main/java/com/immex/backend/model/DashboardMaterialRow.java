package com.immex.backend.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardMaterialRow(
    String material,
    String pedimento,
    String rfc,
    String partida,
    String unidad,
    String regimenAduanero,
    long saldo,
    long saldoFisico,
    long saldoAduaneroPendiente,
    LocalDate fechaVencimiento,
    BigDecimal montoEnRiesgo,
    long diasRestantes,
    String estado) {}