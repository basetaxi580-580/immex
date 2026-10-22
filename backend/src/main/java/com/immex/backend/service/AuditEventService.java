package com.immex.backend.service;

import com.immex.backend.model.AuditEvent;
import com.immex.backend.repository.AuditEventRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AuditEventService {
  private final AuditEventRepository repository;

  public AuditEventService(AuditEventRepository repository) {
    this.repository = repository;
  }

  public AuditEvent record(String entityType, String entityId, String action, String actor, String reason, String details) {
    return repository.save(new AuditEvent(
        null,
        entityType,
        entityId,
        action,
        safeActor(actor),
        safeReason(reason),
        details,
        Instant.now()));
  }

  public List<AuditEvent> findAll() {
    return repository.findAll();
  }

  private String safeActor(String value) {
    String trimmed = value == null ? "" : value.trim();
    return trimmed.isEmpty() ? "Sistema" : trimmed;
  }

  private String safeReason(String value) {
    String trimmed = value == null ? "" : value.trim();
    return trimmed.isEmpty() ? "Motivo no informado" : trimmed;
  }
}
