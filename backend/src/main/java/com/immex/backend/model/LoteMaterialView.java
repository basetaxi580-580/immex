package com.immex.backend.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoteMaterialView(
    String id,
    String material,
    String pedimento,
    String razonSocial,
    String rfc,
    String partida,
    String unidad,
    String regimenAduanero,
    long saldo,
    long saldoFisico,
    long saldoAduaneroPendiente,
    LocalDate fechaEntrada,
    LocalDate fechaVencimiento,
    long diasRestantes,
    String estado,
    BigDecimal valorUnitarioMxn,
    String tipoMaterial) {}