package com.immex.backend.model;

import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("lote_material")
public record LoteMaterial(@Id String id, Long saldo, LocalDate fechaEntrada, LocalDate fechaVencimiento) {}
