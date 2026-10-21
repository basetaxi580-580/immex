package com.immex.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("descarga")
public record Descarga(@Id String id) {}
