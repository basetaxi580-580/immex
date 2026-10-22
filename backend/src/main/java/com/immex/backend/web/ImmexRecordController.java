package com.immex.backend.web;

import com.immex.backend.model.CloseMonthlySignRequest;
import com.immex.backend.model.CloseMonthlyStateResponse;
import com.immex.backend.model.CloseMonthlyStatus;
import com.immex.backend.model.ComplianceAssistantResponse;
import com.immex.backend.model.ImmexCloseReport;
import com.immex.backend.service.CloseMonthlyService;
import com.immex.backend.service.ComplianceAssistantService;
import com.immex.backend.service.ImmexRecordService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/records")
public class ImmexRecordController {
  private final ImmexRecordService service;
  private final CloseMonthlyService closeMonthlyService;
  private final ComplianceAssistantService complianceAssistantService;

  public ImmexRecordController(ImmexRecordService service, CloseMonthlyService closeMonthlyService, ComplianceAssistantService complianceAssistantService) {
    this.service = service;
    this.closeMonthlyService = closeMonthlyService;
    this.complianceAssistantService = complianceAssistantService;
  }

  @GetMapping
  public List<com.immex.backend.model.ImmexRecord> all(@RequestParam(required = false) String razonSocial) {
    return service.findAll(razonSocial);
  }

  @GetMapping("/{id}")
  public com.immex.backend.model.ImmexRecord byId(@PathVariable String id) {
    return service.findById(id);
  }

  @GetMapping("/summary")
  public com.immex.backend.model.ImmexDashboardSummary summary(@RequestParam(required = false) String razonSocial) {
    return service.summary(razonSocial);
  }

  @GetMapping("/close-report")
  public ImmexCloseReport closeReport(@RequestParam(required = false) String razonSocial) {
    return service.buildCloseReport(razonSocial);
  }

  @GetMapping("/close-status")
  public CloseMonthlyStateResponse closeStatus(@RequestParam(required = false) String razonSocial, @RequestParam(required = false) String role) {
    return closeMonthlyService.state(razonSocial, role);
  }

  @GetMapping("/close-history")
  public java.util.List<String> closeHistory(@RequestParam(required = false) String razonSocial) {
    return closeMonthlyService.history(razonSocial);
  }

  @PostMapping("/close-step")
  public CloseMonthlyStatus updateCloseStep(@RequestParam(required = false) String razonSocial, @Valid @RequestBody com.immex.backend.model.CloseMonthlyStepUpdateRequest request) {
    return closeMonthlyService.updateStep(razonSocial, request);
  }

  @PostMapping("/close-sign")
  public CloseMonthlyStatus signClose(@RequestParam(required = false) String razonSocial, @RequestParam(required = false) String role, @Valid @RequestBody CloseMonthlySignRequest request) {
    if (!"Especialista".equalsIgnoreCase(currentRole(role)) && !"Gerencia".equalsIgnoreCase(currentRole(role))) {
      throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el rol Especialista o Gerencia puede firmar el cierre");
    }
    return closeMonthlyService.sign(razonSocial, request);
  }

  @GetMapping("/assistant")
  public ComplianceAssistantResponse assistant(@RequestParam(required = false) String razonSocial) {
    return complianceAssistantService.analyze(razonSocial);
  }

  @PostMapping("/peps/simulate")
  public com.immex.backend.model.PepsOperationResult simulatePeps(@Valid @RequestBody com.immex.backend.model.PepsConsumptionRequest request) {
    return service.simulatePeps(request);
  }

  @PostMapping("/peps/apply")
  public com.immex.backend.model.PepsOperationResult applyPeps(@Valid @RequestBody com.immex.backend.model.PepsConsumptionRequest request) {
    return service.applyPeps(request);
  }

  @PostMapping("/peps/consume")
  public com.immex.backend.model.PepsOperationResult consumePeps(@Valid @RequestBody com.immex.backend.model.PepsConsumptionRequest request) {
    return service.applyPeps(request);
  }

  @PostMapping("/peps/revert")
  public com.immex.backend.model.PepsOperationResult revertPeps(@Valid @RequestBody com.immex.backend.model.PepsConsumptionRequest request) {
    return service.revertPeps(request);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public com.immex.backend.model.ImmexRecord create(@Valid @RequestBody com.immex.backend.model.ImmexRecordRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public com.immex.backend.model.ImmexRecord update(@PathVariable String id, @Valid @RequestBody com.immex.backend.model.ImmexRecordRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable String id) {
    service.delete(id);
  }

  private String currentRole(String role) {
    return role == null || role.isBlank() ? "Especialista" : role.trim();
  }
}
