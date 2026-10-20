package com.immex.backend.web;

import com.immex.backend.model.AuditEvent;
import com.immex.backend.service.AuditEventService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
  private final AuditEventService service;

  public AuditController(AuditEventService service) {
    this.service = service;
  }

  @GetMapping
  public List<AuditEvent> all() {
    return service.findAll();
  }
}
