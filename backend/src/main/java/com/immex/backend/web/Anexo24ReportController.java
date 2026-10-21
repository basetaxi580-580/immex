package com.immex.backend.web;

import com.immex.backend.service.Anexo24ReportService;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
public class Anexo24ReportController {
  private static final String XLSX_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  private final Anexo24ReportService service;

  public Anexo24ReportController(Anexo24ReportService service) {
    this.service = service;
  }

  @GetMapping(value = "/anexo24", produces = XLSX_MIME)
  public ResponseEntity<byte[]> download(@RequestParam(required = false) String razonSocial, @RequestParam(required = false) String mes) {
    byte[] bytes = service.generate(razonSocial, mes);
    String filename = "anexo24" + (mes != null && !mes.isBlank() ? "-" + mes.trim() : "") + ".xlsx";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(XLSX_MIME));
    headers.setContentDisposition(ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
    headers.setContentLength(bytes.length);
    return ResponseEntity.ok().headers(headers).body(bytes);
  }
}
