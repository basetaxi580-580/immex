package com.immex.backend.model;

import java.math.BigDecimal;
import java.util.List;

public record DashboardInventorySummary(BigDecimal saldoTotalMxn, BigDecimal montoVencidoMxn, BigDecimal montoPorVencerMxn, List<DashboardMaterialRow> materiales) {}
