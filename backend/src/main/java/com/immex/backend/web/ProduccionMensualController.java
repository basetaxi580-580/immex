package com.immex.backend.web;

import com.immex.backend.model.ProduccionMensualRequest;
import com.immex.backend.model.ProduccionMensualResult;
import com.immex.backend.service.ProduccionMensualService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/produccion")
public class ProduccionMensualController {
  private final ProduccionMensualService service;

  public ProduccionMensualController(ProduccionMensualService service) {
    this.service = service;
  }

  @PostMapping("/preview")
  public ProduccionMensualResult preview(@Valid @RequestBody ProduccionMensualRequest request) {
    return service.preview(request);
  }

  @PostMapping("/mensual")
  @ResponseStatus(HttpStatus.CREATED)
  public ProduccionMensualResult mensual(@Valid @RequestBody ProduccionMensualRequest request) {
    return service.ejecutar(request);
  }
}
