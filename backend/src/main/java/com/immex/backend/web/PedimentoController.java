package com.immex.backend.web;

import com.immex.backend.model.PedimentoImportRequest;
import com.immex.backend.model.PedimentoImportResult;
import com.immex.backend.service.PedimentoImportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedimentos")
public class PedimentoController {
  private final PedimentoImportService service;

  public PedimentoController(PedimentoImportService service) {
    this.service = service;
  }

  @PostMapping("/import")
  @ResponseStatus(HttpStatus.CREATED)
  public PedimentoImportResult importPedimento(@Valid @RequestBody PedimentoImportRequest request) {
    return service.importPedimento(request);
  }
}
