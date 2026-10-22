package com.immex.backend.web;

import com.immex.backend.model.ComplianceAssistantResponse;
import com.immex.backend.service.ComplianceAssistantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
public class ComplianceAssistantController {
  private final ComplianceAssistantService service;

  public ComplianceAssistantController(ComplianceAssistantService service) {
    this.service = service;
  }

  @GetMapping("/compliance")
  public ComplianceAssistantResponse compliance(@RequestParam(required = false) String razonSocial) {
    return service.analyze(razonSocial);
  }
}
