package com.immex.backend.web;

import com.immex.backend.model.ImmexCloseReport;
import com.immex.backend.model.ImmexDashboardSummary;
import com.immex.backend.model.ImmexRecord;
import com.immex.backend.model.ImmexRecordRequest;
import com.immex.backend.model.PepsConsumptionRequest;
import com.immex.backend.model.PepsOperationResult;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/records")
public class ImmexRecordController {
  private final ImmexRecordService service;

  public ImmexRecordController(ImmexRecordService service) {
    this.service = service;
  }

  @GetMapping
  public List<ImmexRecord> all() {
    return service.findAll();
  }

  @GetMapping("/{id}")
  public ImmexRecord byId(@PathVariable String id) {
    return service.findById(id);
  }

  @GetMapping("/summary")
  public ImmexDashboardSummary summary() {
    return service.summary();
  }

  @GetMapping("/close-report")
  public ImmexCloseReport closeReport() {
    return service.buildCloseReport();
  }

  @PostMapping("/peps/consume")
  public PepsOperationResult consumePeps(@Valid @RequestBody PepsConsumptionRequest request) {
    return service.consumePeps(request);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ImmexRecord create(@Valid @RequestBody ImmexRecordRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public ImmexRecord update(@PathVariable String id, @Valid @RequestBody ImmexRecordRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable String id) {
    service.delete(id);
  }
}
