package com.immex.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("orden_produccion")
public record OrdenProduccion(@Id String id) {}
