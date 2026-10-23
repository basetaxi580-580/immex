package com.immex.backend.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConciliacionRow(
    String source,
    String pedimento,
    String partida,
    String material,
    String rfc,
    BigDecimal cantidad,
    LocalDate fecha) {}