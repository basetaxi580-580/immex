package com.immex.backend.web;

import com.immex.backend.model.ExportacionRequest;
import com.immex.backend.model.ExportacionResult;
import com.immex.backend.service.ExportacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exportaciones")
public class ExportacionController {
  private final ExportacionService service;

  public ExportacionController(ExportacionService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ExportacionResult create(@Valid @RequestBody ExportacionRequest request) {
    return service.create(request);
  }
}
