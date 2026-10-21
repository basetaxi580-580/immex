package com.immex.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("evento_auditoria")
public record EventoAuditoria(@Id String id) {}
