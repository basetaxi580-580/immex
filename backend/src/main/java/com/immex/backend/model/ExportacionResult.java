package com.immex.backend.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExportacionResult(String pedimentoExportacion, LocalDate fecha, String producto, int cantidad, BigDecimal totalExportado, List<String> lotesConsumidos, boolean completado, String message) {}
