package com.immex.backend.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardMaterialRow(
    String material,
    long saldo,
    LocalDate fechaVencimiento,
    BigDecimal montoEnRiesgo,
    long diasRestantes,
    String estado) {}
