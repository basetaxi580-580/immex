package com.immex.backend.model;

import java.time.Instant;

public record ConciliacionExceptionItem(
    String id,
    String source,
    String pedimento,
    String partida,
    String material,
    String rfc,
    String tipo,
    String detalle,
    String estado,
    String responsable,
    String resolucion,
    Instant createdAt,
    Instant updatedAt) {}