package com.immex.backend.model;

import java.math.BigDecimal;
import java.util.List;

public record ProduccionMensualLine(String material, BigDecimal cantidadBom, BigDecimal mermaIncluida, BigDecimal requerido, BigDecimal descargado, BigDecimal saldoPendiente, List<String> lotesAfectados) {}
