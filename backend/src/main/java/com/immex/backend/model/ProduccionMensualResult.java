package com.immex.backend.model;

import java.math.BigDecimal;
import java.util.List;

public record ProduccionMensualResult(String producto, int cantidadProducida, BigDecimal mermaPct, List<ProduccionMensualLine> lineas, int movimientosDescarga, boolean completado, String message) {}
