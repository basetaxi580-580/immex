package com.immex.backend.service;

import com.immex.backend.model.AuditEvent;
import com.immex.backend.repository.AuditEventRepository;
import java.time.Instant;
import java.util.List;
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
        actor,
        reason,
        details,
        Instant.now()));
  }

  public List<AuditEvent> findAll() {
    return repository.findAll();
  }
}
