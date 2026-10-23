package com.immex.backend.web;

import com.immex.backend.model.ConciliacionExceptionItem;
import com.immex.backend.model.ConciliacionResolveRequest;
import com.immex.backend.model.ConciliacionResult;
import com.immex.backend.model.ConciliacionRow;
import com.immex.backend.service.ConciliacionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/conciliacion")
public class ConciliacionController {
  private final ConciliacionService service;

  public ConciliacionController(ConciliacionService service) {
    this.service = service;
  }

  @PostMapping("/import")
  @ResponseStatus(HttpStatus.CREATED)
  public List<ConciliacionRow> importFile(@RequestParam String source, @RequestPart("file") MultipartFile file) {
    return service.importFile(source, file);
  }

  @GetMapping("/preview")
  public List<ConciliacionRow> preview(@RequestParam String source) {
    return service.previewSource(source);
  }

  @PostMapping("/reconcile")
  public ConciliacionResult reconcile() {
    return service.reconcile();
  }

  @GetMapping("/exceptions")
  public List<ConciliacionExceptionItem> exceptions() {
    return service.listExceptions();
  }

  @PostMapping("/exceptions/{id}/resolve")
  public ConciliacionExceptionItem resolve(@PathVariable String id, @Valid @RequestBody ConciliacionResolveRequest request) {
    return service.resolve(id, request);
  }
}
