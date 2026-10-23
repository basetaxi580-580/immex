package com.immex.backend.service;

import com.immex.backend.model.CloseMonthlyChangeEntry;
import com.immex.backend.model.CloseMonthlyHistoryItem;
import com.immex.backend.model.CloseMonthlySignRequest;
import com.immex.backend.model.CloseMonthlyStateResponse;
import com.immex.backend.model.CloseMonthlyStatus;
import com.immex.backend.model.CloseMonthlyStepUpdateRequest;
import com.immex.backend.model.CloseWorkflowStep;
import com.immex.backend.model.ImmexCloseReport;
import java.time.Instant;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CloseMonthlyService {
  private static final String COLLECTION = "cierre_mensual";
  private final MongoTemplate mongoTemplate;
  private final AuditEventService auditEventService;
  private final ImmexRecordService recordService;
  private final ConciliacionService conciliacionService;

  public CloseMonthlyService(MongoTemplate mongoTemplate, AuditEventService auditEventService, ImmexRecordService recordService, ConciliacionService conciliacionService) {
    this.mongoTemplate = mongoTemplate;
    this.auditEventService = auditEventService;
    this.recordService = recordService;
    this.conciliacionService = conciliacionService;
  }

  public CloseMonthlyStateResponse state(String razonSocial, String role) {
    CloseMonthlyStatus status = status(razonSocial);
    boolean canSign = hasSigningRole(role) && status.canClose() && !status.hasCriticalErrors();
    return new CloseMonthlyStateResponse(status, canSign, status.firmado());
  }

  public CloseMonthlyStatus status(String razonSocial) {
    ensureCollection();
    String key = keyOf(razonSocial);
    Document doc = loadOrCreate(key);
    return toStatus(doc);
  }

  public CloseMonthlyStatus updateStep(String razonSocial, CloseMonthlyStepUpdateRequest request) {
    ensureCollection();
    String key = keyOf(razonSocial);
    Document doc = loadOrCreate(key);
    List<Document> steps = asDocumentList(doc.get("steps"));
    Document step = steps.stream()
        .filter(item -> request.getStepKey().equals(item.getString("key")))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Etapa no encontrada"));
    String oldState = stringValue(step.get("estado"));
    step.put("estado", "Completada");
    step.put("responsable", request.getActor().trim());
    step.put("detalle", request.getDetail() == null ? stringValue(step.get("detalle")) : request.getDetail().trim());
    step.put("updatedBy", request.getActor().trim());
    step.put("updatedAt", Instant.now().toString());
    appendHistory(doc, request.getStepKey(), request.getActor(), "estado", oldState, "Completada", request.getReason());
    save(doc);
    auditEventService.record("CierreMensual", key, "STEP_UPDATE", request.getActor(), request.getReason(), request.getStepKey() + " => Completada");
    return toStatus(doc);
  }

  public CloseMonthlyStatus sign(String razonSocial, CloseMonthlySignRequest request) {
    ensureCollection();
    String key = keyOf(razonSocial);
    Document doc = loadOrCreate(key);
    CloseMonthlyStatus current = toStatus(doc);
    if (current.hasCriticalErrors() || !current.canClose()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede cerrar mientras existan errores críticos o pasos pendientes");
    }
    Instant now = Instant.now();
    doc.put("firmado", true);
    doc.put("firmadoPor", request.getActor().trim());
    doc.put("firmadoAt", now.toString());
    appendHistory(doc, "close", request.getActor(), "firmado", String.valueOf(current.firmado()), "true", "Aprobación final del cierre mensual");
    save(doc);
    auditEventService.record("CierreMensual", key, "FIRMAR_CIERRE", request.getActor(), "Aprobación final", "periodo=" + currentPeriod());
    return toStatus(doc);
  }

  public List<CloseMonthlyHistoryItem> history(String razonSocial) {
    ensureCollection();
    Document doc = loadOrCreate(keyOf(razonSocial));
    return asDocumentList(doc.get("history")).stream().map(this::toHistoryItem).toList();
  }

  public CloseMonthlyStateResponse stateResponse(String razonSocial, String role) {
    return state(razonSocial, role);
  }

  public void requireSigned(String razonSocial) {
    if (!status(razonSocial).firmado()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "El cierre mensual no está firmado");
    }
  }

  public void requireSigned() {
    requireSigned("all");
  }

  public void requireOpen(String razonSocial) {
    if (status(razonSocial).firmado()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "El cierre mensual de este periodo ya está firmado; no se pueden agregar importaciones.");
    }
  }

  public boolean hasCriticalErrors(String razonSocial) {
    return status(razonSocial).hasCriticalErrors();
  }

  public boolean hasCriticalErrors() {
    return hasCriticalErrors("all");
  }

  private CloseMonthlyStatus toStatus(Document doc) {
    List<Document> stepsDocs = asDocumentList(doc.get("steps"));
    List<Document> historyDocs = asDocumentList(doc.get("history"));
    List<CloseWorkflowStep> steps = stepsDocs.stream().map(this::toStep).toList();
    List<CloseMonthlyChangeEntry> history = historyDocs.stream().map(this::toChangeEntry).toList();
    boolean canClose = steps.stream().allMatch(step -> "Completada".equalsIgnoreCase(step.estado())) && !hasCriticalIssues(doc);
    int pendingCount = (int) steps.stream().filter(step -> !"Completada".equalsIgnoreCase(step.estado())).count();
    int progressPct = steps.isEmpty() ? 0 : (int) Math.round((steps.size() - pendingCount) * 100.0 / steps.size());
    List<String> pendingItems = steps.stream()
        .filter(step -> !"Completada".equalsIgnoreCase(step.estado()))
        .map(step -> step.label() + " — " + step.pendiente())
        .toList();
    boolean hasCriticalErrors = hasCriticalIssues(doc);
    String currentResponsible = steps.stream().filter(step -> !"Completada".equalsIgnoreCase(step.estado())).map(CloseWorkflowStep::responsable).filter(v -> v != null && !v.isBlank()).findFirst().orElse("Especialista aduanal");
    return new CloseMonthlyStatus(
        stringValue(doc.getOrDefault("periodo", currentPeriod())),
        Boolean.TRUE.equals(doc.get("firmado")),
        stringValue(doc.get("firmadoPor")),
        doc.get("firmadoAt") == null ? null : Instant.parse(String.valueOf(doc.get("firmadoAt"))),
        canClose,
        hasCriticalErrors,
        progressPct,
        pendingCount,
        pendingItems,
        steps,
        history,
        currentResponsible);
  }

  private void ensureCollection() {
    if (!mongoTemplate.collectionExists(COLLECTION)) {
      mongoTemplate.createCollection(COLLECTION);
    }
  }

  private boolean hasCriticalIssues(Document doc) {
    Document report = buildReportSummary();
    long openRecords = asLong(report.get("openRecords"));
    long expiringSoon = asLong(report.get("expiringSoonCount"));
    long unresolved = countUnresolvedExceptions();
    return openRecords > 0 || expiringSoon > 0 || unresolved > 0;
  }

  private Document buildReportSummary() {
    ImmexCloseReport report = recordService.buildCloseReport("all");
    return new Document("openRecords", report.getOpenRecords())
        .append("expiringSoonCount", report.getExpiringSoonCount())
        .append("warnings", report.getWarnings())
        .append("pendingItems", report.getPendingItems())
        .append("estado", report.getEstado());
  }

  private long countUnresolvedExceptions() {
    if (!mongoTemplate.collectionExists("conciliacion_excepcion")) {
      return 0L;
    }
    return mongoTemplate.getCollection("conciliacion_excepcion").countDocuments(new Document("estado", new Document("$ne", "Resuelta")));
  }

  private Document loadOrCreate(String key) {
    Document doc = mongoTemplate.getCollection(COLLECTION).find(new Document("razonSocial", key).append("periodo", currentPeriod())).first();
    if (doc != null) {
      return doc;
    }
    Instant now = Instant.now();
    List<Document> steps = defaultSteps();
    Document created = new Document("razonSocial", key)
        .append("periodo", currentPeriod())
        .append("firmado", false)
        .append("steps", steps)
        .append("history", new ArrayList<Document>())
        .append("createdAt", now.toString())
        .append("updatedAt", now.toString());
    mongoTemplate.getCollection(COLLECTION).insertOne(created);
    return created;
  }

  private List<Document> defaultSteps() {
    List<Document> steps = new ArrayList<>();
    steps.add(step("load", "1. Cargar y validar información", "Captura y validación de archivos", "Cargar archivos, formatos y duplicados", "Especialista aduanal"));
    steps.add(step("reconcile", "2. Conciliar movimientos", "Conciliación contra agente y registros internos", "Cruzar pedimentos, cantidades y fechas", "Especialista aduanal"));
    steps.add(step("calculate", "3. Calcular consumos y descargos", "Cálculo de PEPS, consumos y descargos", "Revisar movimientos y saldos", "Especialista aduanal"));
    steps.add(step("resolve", "4. Resolver excepciones", "Excepciones abiertas", "Resolver faltantes y diferencias", "Especialista aduanal"));
    steps.add(step("approve", "5. Revisar y aprobar", "Revisión final y firma", "Validar resultado del cierre", "Gerencia / Especialista"));
    steps.add(step("reports", "6. Generar reportes", "Emisión Anexo 24 y evidencia", "Generar archivos y resguardar evidencia", "Sistema"));
    return steps;
  }

  private Document step(String key, String label, String responsable, String pendiente, String actor) {
    return new Document("key", key)
        .append("label", label)
        .append("responsable", responsable)
        .append("estado", "Pendiente")
        .append("pendiente", pendiente)
        .append("detalle", pendiente)
        .append("updatedBy", actor)
        .append("updatedAt", Instant.now().toString());
  }

  private CloseWorkflowStep toStep(Document doc) {
    return new CloseWorkflowStep(
        stringValue(doc.get("key")),
        stringValue(doc.get("label")),
        stringValue(doc.get("responsable")),
        stringValue(doc.get("estado")),
        stringValue(doc.get("pendiente")),
        stringValue(doc.get("detalle")),
        stringValue(doc.get("updatedBy")),
        doc.get("updatedAt") == null ? null : Instant.parse(String.valueOf(doc.get("updatedAt"))));
  }

  private CloseMonthlyHistoryItem toHistoryItem(Document doc) {
    return new CloseMonthlyHistoryItem(
        stringValue(doc.get("stepKey")),
        stringValue(doc.get("stepLabel")),
        stringValue(doc.get("actor")),
        doc.get("changedAt") == null ? null : Instant.parse(String.valueOf(doc.get("changedAt"))),
        stringValue(doc.get("field")),
        stringValue(doc.get("oldValue")),
        stringValue(doc.get("newValue")),
        stringValue(doc.get("reason")));
  }

  private CloseMonthlyChangeEntry toChangeEntry(Document doc) {
    return new CloseMonthlyChangeEntry(
        stringValue(doc.get("stepKey")),
        stringValue(doc.get("stepLabel")),
        stringValue(doc.get("actor")),
        doc.get("changedAt") == null ? null : Instant.parse(String.valueOf(doc.get("changedAt"))),
        stringValue(doc.get("field")),
        stringValue(doc.get("oldValue")),
        stringValue(doc.get("newValue")),
        stringValue(doc.get("reason")));
  }

  private void appendHistory(Document doc, String stepKey, String actor, String field, String oldValue, String newValue, String reason) {
    List<Document> history = asDocumentList(doc.get("history"));
    String stepLabel = stepKey;
    for (Document step : asDocumentList(doc.get("steps"))) {
      if (Objects.equals(stepKey, step.getString("key"))) {
        stepLabel = step.getString("label");
        break;
      }
    }
    history.add(new Document("stepKey", stepKey)
        .append("stepLabel", stepLabel)
        .append("actor", actor)
        .append("changedAt", Instant.now().toString())
        .append("field", field)
        .append("oldValue", oldValue)
        .append("newValue", newValue)
        .append("reason", reason));
    doc.put("history", history);
    doc.put("updatedAt", Instant.now().toString());
  }

  private void save(Document doc) {
    mongoTemplate.getCollection(COLLECTION).replaceOne(new Document("razonSocial", doc.getString("razonSocial")).append("periodo", doc.getString("periodo")), doc, new com.mongodb.client.model.ReplaceOptions().upsert(true));
  }

  private List<Document> asDocumentList(Object value) {
    if (value instanceof List<?> list) {
      List<Document> result = new ArrayList<>();
      for (Object item : list) {
        if (item instanceof Document doc) {
          result.add(doc);
        } else if (item instanceof Map<?, ?> map) {
          Document doc = new Document();
          for (Map.Entry<?, ?> entry : map.entrySet()) {
            doc.append(String.valueOf(entry.getKey()), entry.getValue());
          }
          result.add(doc);
        }
      }
      return result;
    }
    return new ArrayList<>();
  }

  private boolean hasSigningRole(String role) {
    return role != null && !role.isBlank() && ("Especialista".equalsIgnoreCase(role) || "Gerencia".equalsIgnoreCase(role));
  }

  private String keyOf(String razonSocial) {
    String value = razonSocial == null ? "" : razonSocial.trim();
    return value.isEmpty() || "all".equalsIgnoreCase(value) ? "all" : value;
  }

  private String currentPeriod() {
    return YearMonth.now().toString();
  }

  private String stringValue(Object value) {
    return value == null ? null : value.toString();
  }

  private long asLong(Object value) {
    if (value == null) return 0L;
    if (value instanceof Number number) return number.longValue();
    try { return Long.parseLong(value.toString()); } catch (Exception ex) { return 0L; }
  }
}
