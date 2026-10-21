package com.immex.backend.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("evento_auditoria")
public class AuditEvent {
  @Id
  private String id;
  private String entityType;
  private String entityId;
  private String action;
  private String actor;
  private String reason;
  private String details;
  private Instant createdAt;

  public AuditEvent() {}

  public AuditEvent(String id, String entityType, String entityId, String action, String actor, String reason, String details, Instant createdAt) {
    this.id = id;
    this.entityType = entityType;
    this.entityId = entityId;
    this.action = action;
    this.actor = actor;
    this.reason = reason;
    this.details = details;
    this.createdAt = createdAt;
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getEntityType() { return entityType; }
  public void setEntityType(String entityType) { this.entityType = entityType; }
  public String getEntityId() { return entityId; }
  public void setEntityId(String entityId) { this.entityId = entityId; }
  public String getAction() { return action; }
  public void setAction(String action) { this.action = action; }
  public String getActor() { return actor; }
  public void setActor(String actor) { this.actor = actor; }
  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }
  public String getDetails() { return details; }
  public void setDetails(String details) { this.details = details; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
