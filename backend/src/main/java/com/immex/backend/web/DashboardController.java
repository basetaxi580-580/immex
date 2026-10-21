package com.immex.backend.web;

import com.immex.backend.model.DashboardInventorySummary;
import com.immex.backend.service.InventoryDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
  private final InventoryDashboardService service;

  public DashboardController(InventoryDashboardService service) {
    this.service = service;
  }

  @GetMapping("/inventory")
  public DashboardInventorySummary inventory() {
    return service.inventory();
  }
}
