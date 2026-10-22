package com.immex.backend.web;

import com.immex.backend.model.LoteMaterialView;
import com.immex.backend.service.InventoryDashboardService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lotes")
public class LoteController {
  private final InventoryDashboardService service;

  public LoteController(InventoryDashboardService service) {
    this.service = service;
  }

  @GetMapping
  public List<LoteMaterialView> all(@RequestParam(required = false) String material, @RequestParam(required = false) String razonSocial) {
    return service.materialViews(material, razonSocial);
  }
}
