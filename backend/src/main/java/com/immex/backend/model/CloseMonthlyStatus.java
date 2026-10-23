package com.immex.backend.model;

import java.time.Instant;
import java.util.List;
import com.immex.backend.model.CloseMonthlyChangeEntry;

public record CloseMonthlyStatus(
    String periodo,
    boolean firmado,
    String firmadoPor,
    Instant firmadoAt,
    boolean canClose,
    boolean hasCriticalErrors,
    int progressPct,
    int pendingCount,
    List<String> pendingItems,
    List<CloseWorkflowStep> steps,
    List<CloseMonthlyChangeEntry> history,
    String currentResponsible) {}
