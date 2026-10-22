package com.immex.backend.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoteMaterialView(
    String id,
    String material,
    String pedimento,
    String razonSocial,
    long saldo,
    LocalDate fechaEntrada,
    LocalDate fechaVencimiento,
    long diasRestantes,
    String estado,
    BigDecimal valorUnitarioMxn,
    String tipoMaterial) {}
