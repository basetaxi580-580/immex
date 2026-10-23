package com.immex.backend.service;

import com.immex.backend.model.CloseMonthlyStatus;
import com.immex.backend.model.ComplianceAssistantResponse;
import com.immex.backend.model.ComplianceRecommendation;
import com.immex.backend.model.ConciliacionExceptionItem;
import com.immex.backend.model.DashboardInventorySummary;
import com.immex.backend.model.DashboardMaterialRow;
import com.immex.backend.model.ImmexRecord;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class ComplianceAssistantService {
  private final InventoryDashboardService inventoryDashboardService;
  private final ImmexRecordService recordService;
  private final ConciliacionService conciliacionService;
  private final CloseMonthlyService closeMonthlyService;

  public ComplianceAssistantService(
      InventoryDashboardService inventoryDashboardService,
      ImmexRecordService recordService,
      ConciliacionService conciliacionService,
      CloseMonthlyService closeMonthlyService) {
    this.inventoryDashboardService = inventoryDashboardService;
    this.recordService = recordService;
    this.conciliacionService = conciliacionService;
    this.closeMonthlyService = closeMonthlyService;
  }

  public ComplianceAssistantResponse analyze(String razonSocial) {
    DashboardInventorySummary inventory = inventoryDashboardService.inventory(razonSocial);
    List<ImmexRecord> records = recordService.findAll(razonSocial);
    List<ConciliacionExceptionItem> exceptions = conciliacionService.listExceptions();
    CloseMonthlyStatus close = closeMonthlyService.status(razonSocial);

    List<ComplianceRecommendation> recommendations = new ArrayList<>();
    List<String> facts = new ArrayList<>();
    List<String> limitations = new ArrayList<>();

    addVencimientos(inventory, recommendations, facts);
    addSaldoSinDescargar(inventory, records, recommendations, facts);
    addConciliacion(exceptions, recommendations, facts);
    addErroresYFaltantes(records, close, recommendations, facts, limitations);

    String summary = recommendations.isEmpty()
        ? "No se detectaron alertas críticas con la información disponible."
        : "Se detectaron " + recommendations.size() + " alertas de cumplimiento basadas en datos reales de la plataforma.";

    if (!close.canClose()) {
      limitations.add("El cierre mensual todavía no está listo para firma.");
    }
    if (close.hasCriticalErrors()) {
      limitations.add("Existen errores críticos o pasos pendientes que impiden cerrar.");
    }
    limitations.add("El asistente no modifica registros ni ejecuta descargos; solo explica hallazgos y sugiere acciones.");

    return new ComplianceAssistantResponse(scopeLabel(razonSocial), summary, recommendations, facts, limitations);
  }

  private void addVencimientos(DashboardInventorySummary inventory, List<ComplianceRecommendation> recommendations, List<String> facts) {
    if (inventory == null) {
      return;
    }
    List<DashboardMaterialRow> urgent = inventory.materiales().stream()
        .filter(row -> row.diasRestantes() <= 90)
        .sorted(Comparator.comparingLong(DashboardMaterialRow::diasRestantes))
        .toList();
    if (!urgent.isEmpty()) {
      DashboardMaterialRow first = urgent.get(0);
      facts.add("Hay " + urgent.size() + " pedimentos/materiales con vencimiento en 90 días o menos.");
      recommendations.add(new ComplianceRecommendation(
          "Pedimentos próximos a vencer",
          "La plataforma muestra materiales con fecha límite dentro de la ventana crítica.",
          "Aumenta el riesgo de incumplimiento si no se revisan hoy mismo.",
          "Revisa primero el material " + first.material() + " y confirma si procede cierre, descarga o corrección documental.",
          "Pedimento " + first.pedimento() + ", RFC " + safe(first.rfc()) + ", días restantes " + first.diasRestantes(),
          true));
    }
  }

  private void addSaldoSinDescargar(DashboardInventorySummary inventory, List<ImmexRecord> records, List<ComplianceRecommendation> recommendations, List<String> facts) {
    if (inventory == null) {
      return;
    }
    List<DashboardMaterialRow> saldoPendiente = inventory.materiales().stream()
        .filter(row -> row.saldoAduaneroPendiente() > 0)
        .sorted(Comparator.comparingLong(DashboardMaterialRow::saldoAduaneroPendiente).reversed())
        .toList();
    if (!saldoPendiente.isEmpty()) {
      DashboardMaterialRow first = saldoPendiente.get(0);
      facts.add("Hay " + saldoPendiente.size() + " materiales con saldo aduanero pendiente de descarga.");
      recommendations.add(new ComplianceRecommendation(
          "Saldos sin descargar",
          "Existen saldos aduaneros pendientes en inventario real.",
          "Pueden acumularse diferencias entre inventario físico y saldo aduanero.",
          "Valida si el saldo corresponde a producción, exportación o ajuste pendiente antes de autorizar un movimiento.",
          "Material " + first.material() + ", pedimento " + first.pedimento() + ", saldo pendiente " + first.saldoAduaneroPendiente(),
          true));
    }
  }

  private void addConciliacion(List<ConciliacionExceptionItem> exceptions, List<ComplianceRecommendation> recommendations, List<String> facts) {
    List<ConciliacionExceptionItem> open = exceptions.stream()
        .filter(item -> !"Resuelta".equalsIgnoreCase(item.estado()))
        .toList();
    if (!open.isEmpty()) {
      ConciliacionExceptionItem first = open.get(0);
      long diferencias = open.stream().filter(item -> "DIFERENCIA".equalsIgnoreCase(item.tipo())).count();
      long faltantes = open.stream().filter(item -> "FALTANTE".equalsIgnoreCase(item.tipo())).count();
      facts.add("Hay " + open.size() + " excepciones de conciliación abiertas.");
      recommendations.add(new ComplianceRecommendation(
          "Diferencias de conciliación",
          "La conciliación tiene excepciones abiertas en datos reales.",
          "Afecta la consistencia entre archivo aduanero, agente aduanal y registros internos.",
          "Prioriza resolver primero las diferencias de cantidades, fechas y pedimentos antes de firmar el cierre.",
          "Abiertas: " + open.size() + ", diferencias: " + diferencias + ", faltantes: " + faltantes + ", ejemplo: " + first.detalle(),
          true));
    }
  }

  private void addErroresYFaltantes(List<ImmexRecord> records, CloseMonthlyStatus close, List<ComplianceRecommendation> recommendations, List<String> facts, List<String> limitations) {
    List<String> missing = new ArrayList<>();
    records.stream().filter(record -> isBlank(record.getRfc())).findFirst().ifPresent(record -> missing.add("RFC faltante en pedimento " + record.getPedimento()));
    records.stream().filter(record -> isBlank(record.getPartida())).findFirst().ifPresent(record -> missing.add("Partida faltante en pedimento " + record.getPedimento()));
    records.stream().filter(record -> isBlank(record.getUnidad())).findFirst().ifPresent(record -> missing.add("Unidad faltante en pedimento " + record.getPedimento()));
    records.stream().filter(record -> isBlank(record.getFechaIngreso() == null ? null : record.getFechaIngreso().toString())).findFirst().ifPresent(record -> missing.add("Fecha de ingreso faltante en pedimento " + record.getPedimento()));

    if (!missing.isEmpty()) {
      facts.add("Hay datos faltantes en registros operativos.");
      recommendations.add(new ComplianceRecommendation(
          "Datos faltantes",
          "La plataforma tiene campos obligatorios sin completar en algunos registros.",
          "Esto impide validar cálculos y trazabilidad completa.",
          "Completa los campos obligatorios antes de continuar con el cierre y vuelve a validar.",
          String.join("; ", missing),
          true));
    }

    if (close.hasCriticalErrors()) {
      recommendations.add(new ComplianceRecommendation(
          "Errores críticos de cierre",
          "El flujo de cierre indica errores críticos o pasos pendientes.",
          "No se puede cerrar con seguridad ni emitir aprobación final.",
          "Corrige primero las etapas pendientes que el cierre marca como bloqueantes y luego reintenta la aprobación.",
          "Avance actual " + close.progressPct() + "% · pendientes " + close.pendingCount(),
          true));
    }
  }

  private String scopeLabel(String razonSocial) {
    String value = razonSocial == null || razonSocial.isBlank() ? "Todas las razones sociales" : razonSocial.trim();
    return value;
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private String safe(String value) {
    return value == null || value.isBlank() ? "No calculable" : value;
  }
}
