package com.immex.backend.model;

import java.time.Instant;

public record CloseMonthlyChangeEntry(
    String stepKey,
    String stepLabel,
    String actor,
    Instant changedAt,
    String field,
    String oldValue,
    String newValue,
    String reason) {}
