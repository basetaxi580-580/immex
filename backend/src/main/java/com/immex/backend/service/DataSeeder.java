package com.immex.backend.service;

import com.immex.backend.model.ImmexRecord;
import com.immex.backend.repository.ImmexRecordRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {
  @Bean
  CommandLineRunner seed(ImmexRecordRepository repository) {
    return args -> {
      List<ImmexRecord> samples = List.of(
          new ImmexRecord(null, "PI-2601-001", "Resina ABS natural", "Proveedor Norte", "Entrada", "Aerosistemas del Norte, S.A. de C.V.", 1200, new BigDecimal("38.50"), LocalDate.of(2026, 8, 20), LocalDate.of(2028, 2, 20), "Vigente", true, "Planta de inyección; merma 4.2% y recuperación como regrind", "Sistema", Instant.now(), Instant.now()),
          new ImmexRecord(null, "PI-2601-002", "Regranulado ABS reciclado", "Reciclados Industriales", "Entrada", "Aerosistemas del Norte, S.A. de C.V.", 180, new BigDecimal("12.90"), LocalDate.of(2025, 7, 15), LocalDate.of(2027, 1, 15), "Por vencer", true, "Material reciclado interno para nuevas corridas", "Sistema", Instant.now(), Instant.now()),
          new ImmexRecord(null, "PI-2601-003", "Pigmento masterbatch negro", "Colorantes del Centro", "Entrada", "Aerosistemas del Norte, S.A. de C.V.", 40, new BigDecimal("95.00"), LocalDate.of(2024, 12, 1), LocalDate.of(2026, 6, 1), "Vencido", true, "Lote detenido para disposición documental", "Sistema", Instant.now(), Instant.now()),
          new ImmexRecord(null, "AR-2601-101", "Conector 12 pines", "Proveedor Eléctrico", "Entrada", "Componentes Industriales del Bajío, S.A. de C.V.", 12000, new BigDecimal("3.20"), LocalDate.of(2026, 8, 1), LocalDate.of(2028, 2, 1), "Vigente", true, "BOM principal para arnés automotriz", "Sistema", Instant.now(), Instant.now()),
          new ImmexRecord(null, "AR-2601-102", "Terminal hembra 6.3 mm", "Terminales del Bajío", "Entrada", "Componentes Industriales del Bajío, S.A. de C.V.", 25000, new BigDecimal("0.48"), LocalDate.of(2025, 7, 20), LocalDate.of(2027, 1, 20), "Por vencer", true, "Stock crítico para línea de arneses", "Sistema", Instant.now(), Instant.now()),
          new ImmexRecord(null, "AR-2601-103", "Cinta PVC automotriz", "Suministros Flexibles", "Entrada", "Componentes Industriales del Bajío, S.A. de C.V.", 9000, new BigDecimal("0.12"), LocalDate.of(2024, 11, 15), LocalDate.of(2026, 5, 15), "Vencido", true, "Material caducado por rotación lenta", "Sistema", Instant.now(), Instant.now()));

      boolean needsReset = repository.count() == 0
          || repository.findAll().stream().anyMatch(record -> record.getPedimento() == null || record.getMaterial() == null);
      if (needsReset) {
        repository.deleteAll();
        repository.saveAll(samples);
        return;
      }

      Set<String> existingPedimentos = repository.findAll().stream()
          .map(ImmexRecord::getPedimento)
          .filter(pedimento -> pedimento != null && !pedimento.isBlank())
          .collect(Collectors.toSet());
      samples.stream()
          .filter(record -> !existingPedimentos.contains(record.getPedimento()))
          .forEach(repository::save);
    };
  }
}
