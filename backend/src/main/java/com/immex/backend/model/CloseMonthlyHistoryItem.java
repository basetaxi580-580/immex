package com.immex.backend.model;

import java.time.Instant;

public record CloseMonthlyHistoryItem(
    String stepKey,
    String stepLabel,
    String actor,
    Instant changedAt,
    String field,
    String oldValue,
    String newValue,
    String reason) {}
