package com.immex.backend.service;

import com.immex.backend.model.ImmexRecord;
import com.immex.backend.repository.ImmexRecordRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {
  @Bean
  CommandLineRunner seed(ImmexRecordRepository repository) {
    return args -> {
      boolean needsReset = repository.count() == 0
          || repository.findAll().stream().anyMatch(record -> record.getPedimento() == null || record.getMaterial() == null);
      if (!needsReset) return;
      repository.deleteAll();
      Instant now = Instant.now();
      repository.save(new ImmexRecord(null, "PI-2401-001", "Resina ABS", "Planta norte", "Entrada", 1200, new BigDecimal("42.50"), LocalDate.now().minusDays(25), LocalDate.now().plusDays(55), "Vigente", true, "Alta inicial para lote de producción", "Sistema", now, now));
      repository.save(new ImmexRecord(null, "PI-2401-002", "Conector 12p", "Proveedor Monterrey", "Entrada", 8000, new BigDecimal("3.20"), LocalDate.now().minusDays(170), LocalDate.now().plusDays(10), "Por vencer", true, "Revisar descarga antes del cierre", "Sistema", now, now));
      repository.save(new ImmexRecord(null, "PO-2401-010", "Arnés eléctrico", "Línea A", "Exportación", 600, new BigDecimal("18.90"), LocalDate.now().minusDays(90), LocalDate.now().plusDays(120), "Cerrado", true, "Exportación conciliada", "Sistema", now, now));
    };
  }
}
