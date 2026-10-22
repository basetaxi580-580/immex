package com.immex.backend.model;

import java.time.Instant;

public record CloseWorkflowStep(
    String key,
    String label,
    String responsable,
    String estado,
    String pendiente,
    String detalle,
    String updatedBy,
    Instant updatedAt) {}
