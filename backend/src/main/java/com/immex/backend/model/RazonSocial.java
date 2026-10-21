package com.immex.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("razon_social")
public record RazonSocial(@Id String id, String rfc) {}
