package com.immex.backend.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PedimentoImportResult(String numeroPedimento, LocalDate fecha, String razonSocial, int partidasCreadas, BigDecimal totalMxn) {}
