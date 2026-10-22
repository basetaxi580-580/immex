package com.immex.backend.service;

import com.immex.backend.model.ConciliacionExceptionItem;
import com.immex.backend.model.ConciliacionResolveRequest;
import com.immex.backend.model.ConciliacionResult;
import com.immex.backend.model.ConciliacionRow;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ConciliacionService {
  private final MongoTemplate mongoTemplate;
  private final AuditEventService auditEventService;

  public ConciliacionService(MongoTemplate mongoTemplate, AuditEventService auditEventService) {
    this.mongoTemplate = mongoTemplate;
    this.auditEventService = auditEventService;
  }

  public List<ConciliacionRow> importFile(String source, MultipartFile file) {
    ensureCollections();
    try {
      String content = new String(file.getBytes());
      List<ConciliacionRow> rows = parseDelimited(source, content);
      String collection = collectionForSource(source);
      if (!mongoTemplate.collectionExists(collection)) {
        mongoTemplate.createCollection(collection);
      }
      mongoTemplate.getCollection(collection).deleteMany(new Document());
      for (ConciliacionRow row : rows) {
        mongoTemplate.insert(new Document("source", source)
            .append("pedimento", row.pedimento())
            .append("partida", row.partida())
            .append("material", row.material())
            .append("rfc", row.rfc())
            .append("cantidad", row.cantidad())
            .append("fecha", row.fecha() == null ? null : row.fecha().toString())
            .append("createdAt", Instant.now().toString()), collection);
      }
      auditEventService.record("Conciliacion", source, "IMPORTAR", "Sistema", "Importación de archivo conciliatorio", "source=" + source + ", registros=" + rows.size());
      return rows;
    } catch (Exception ex) {
      throw new IllegalStateException("No se pudo importar el archivo de conciliación", ex);
    }
  }

  public ConciliacionResult reconcile() {
    ensureCollections();
    List<ConciliacionRow> aduanas = readRows("conciliacion_aduana", "aduana");
    List<ConciliacionRow> internos = readRows("conciliacion_interno", "interno");
    Map<String, ConciliacionRow> aduanaIndex = new HashMap<>();
    Set<String> duplicateKeys = new HashSet<>();
    int duplicados = 0;
    for (ConciliacionRow row : aduanas) {
      String key = key(row);
      if (!duplicateKeys.add(key)) {
        duplicados++;
      }
      aduanaIndex.putIfAbsent(key, row);
    }

    List<ConciliacionExceptionItem> exceptions = new ArrayList<>();
    int faltantes = 0;
    int diferencias = 0;
    Set<String> seenInternal = new HashSet<>();

    for (ConciliacionRow interno : internos) {
      String key = key(interno);
      seenInternal.add(key);
      ConciliacionRow aduana = aduanaIndex.get(key);
      if (aduana == null) {
        faltantes++;
        exceptions.add(createException("FALTANTE", interno, "No existe registro equivalente en archivo aduanero", "Abierta"));
        continue;
      }
      if (compare(interno.cantidad(), aduana.cantidad()) != 0 || !sameDate(interno.fecha(), aduana.fecha())) {
        diferencias++;
        String detalle = "Cantidad interno=" + interno.cantidad() + ", aduana=" + aduana.cantidad() + "; fecha interno=" + interno.fecha() + ", aduana=" + aduana.fecha();
        exceptions.add(createException("DIFERENCIA", interno, detalle, "Abierta"));
      }
    }

    for (ConciliacionRow aduana : aduanas) {
      if (!seenInternal.contains(key(aduana))) {
        faltantes++;
        exceptions.add(createException("EXCEPCION", aduana, "Existe en archivo aduanero pero falta en registros internos", "Abierta"));
      }
    }

    for (ConciliacionExceptionItem item : exceptions) {
      mongoTemplate.insert(new Document("id", item.id())
          .append("source", item.source())
          .append("pedimento", item.pedimento())
          .append("partida", item.partida())
          .append("material", item.material())
          .append("rfc", item.rfc())
          .append("tipo", item.tipo())
          .append("detalle", item.detalle())
          .append("estado", item.estado())
          .append("responsable", item.responsable())
          .append("resolucion", item.resolucion())
          .append("createdAt", item.createdAt().toString())
          .append("updatedAt", item.updatedAt().toString()), "conciliacion_excepcion");
    }

    auditEventService.record("Conciliacion", "archivo", "RECONCILIAR", "Sistema", "Conciliación de archivos aduaneros", "aduana=" + aduanas.size() + ", interno=" + internos.size() + ", excepciones=" + exceptions.size());
    return new ConciliacionResult(aduanas.size() + internos.size(), duplicados, faltantes, diferencias, exceptions.size(), exceptions);
  }

  public List<ConciliacionExceptionItem> listExceptions() {
    ensureCollections();
    return mongoTemplate.getCollection("conciliacion_excepcion").find().into(new ArrayList<>()).stream()
        .map(doc -> new ConciliacionExceptionItem(
            stringValue(doc.get("id")),
            stringValue(doc.get("source")),
            stringValue(doc.get("pedimento")),
            stringValue(doc.get("partida")),
            stringValue(doc.get("material")),
            stringValue(doc.get("rfc")),
            stringValue(doc.get("tipo")),
            stringValue(doc.get("detalle")),
            stringValue(doc.get("estado")),
            stringValue(doc.get("responsable")),
            stringValue(doc.get("resolucion")),
            instantValue(doc.get("createdAt")),
            instantValue(doc.get("updatedAt"))))
        .sorted(Comparator.comparing(ConciliacionExceptionItem::createdAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
        .toList();
  }

  public ConciliacionExceptionItem resolve(String id, ConciliacionResolveRequest request) {
    ensureCollections();
    Document doc = mongoTemplate.getCollection("conciliacion_excepcion").find(new Document("id", id)).first();
    if (doc == null) {
      throw new IllegalStateException("Excepción no encontrada");
    }
    Instant now = Instant.now();
    mongoTemplate.getCollection("conciliacion_excepcion").updateOne(
        new Document("id", id),
        new Document("$set", new Document("estado", "Resuelta")
            .append("responsable", request.getResponsable().trim())
            .append("resolucion", request.getResolucion().trim())
            .append("updatedAt", now.toString())));
    auditEventService.record("Conciliacion", id, "RESOLVER", request.getResponsable().trim(), request.getResolucion().trim(), stringValue(doc.get("detalle")));
    return new ConciliacionExceptionItem(
        id,
        stringValue(doc.get("source")),
        stringValue(doc.get("pedimento")),
        stringValue(doc.get("partida")),
        stringValue(doc.get("material")),
        stringValue(doc.get("rfc")),
        stringValue(doc.get("tipo")),
        stringValue(doc.get("detalle")),
        "Resuelta",
        request.getResponsable().trim(),
        request.getResolucion().trim(),
        instantValue(doc.get("createdAt")),
        now);
  }

  public List<ConciliacionRow> previewSource(String source) {
    ensureCollections();
    String collection = collectionForSource(source);
    if (!mongoTemplate.collectionExists(collection)) {
      return List.of();
    }
    return readRows(collection, source);
  }

  private List<ConciliacionRow> parseDelimited(String source, String content) {
    List<ConciliacionRow> rows = new ArrayList<>();
    String[] lines = content.split("\\R");
    if (lines.length == 0) return rows;
    String[] header = splitLine(lines[0]);
    int pedimentoIdx = indexOf(header, "pedimento", "folio");
    int partidaIdx = indexOf(header, "partida", "linea");
    int materialIdx = indexOf(header, "material", "descripcion");
    int rfcIdx = indexOf(header, "rfc");
    int cantidadIdx = indexOf(header, "cantidad", "qty");
    int fechaIdx = indexOf(header, "fecha", "date");
    if (pedimentoIdx < 0 || partidaIdx < 0 || materialIdx < 0 || rfcIdx < 0 || cantidadIdx < 0) {
      throw new IllegalStateException("Formato de conciliación inválido");
    }
    for (int i = 1; i < lines.length; i++) {
      String line = lines[i].trim();
      if (line.isEmpty()) continue;
      String[] cols = splitLine(line);
      rows.add(new ConciliacionRow(
          source,
          valueAt(cols, pedimentoIdx),
          valueAt(cols, partidaIdx),
          valueAt(cols, materialIdx),
          valueAt(cols, rfcIdx),
          bigDecimalValue(valueAt(cols, cantidadIdx)),
          fechaIdx >= 0 ? localDateValue(valueAt(cols, fechaIdx)) : null));
    }
    return rows;
  }

  private String[] splitLine(String line) {
    return line.contains(";") ? line.split(";", -1) : line.split(",", -1);
  }

  private int indexOf(String[] header, String... names) {
    for (int i = 0; i < header.length; i++) {
      String normalized = normalize(header[i]);
      for (String name : names) {
        if (normalized.equals(normalize(name))) return i;
      }
    }
    return -1;
  }

  private String valueAt(String[] values, int index) {
    return index >= 0 && index < values.length ? values[index].trim() : "";
  }

  private List<ConciliacionRow> readRows(String collection, String source) {
    if (!mongoTemplate.collectionExists(collection)) {
      return List.of();
    }
    return mongoTemplate.getCollection(collection).find().into(new ArrayList<>()).stream()
        .map(doc -> new ConciliacionRow(
            source,
            stringValue(doc.get("pedimento")),
            stringValue(doc.get("partida")),
            stringValue(doc.get("material")),
            stringValue(doc.get("rfc")),
            bigDecimalValue(doc.get("cantidad")),
            localDateValue(doc.get("fecha"))))
        .toList();
  }

  private ConciliacionExceptionItem createException(String tipo, ConciliacionRow row, String detalle, String estado) {
    Instant now = Instant.now();
    return new ConciliacionExceptionItem(
        java.util.UUID.randomUUID().toString(),
        row.source(),
        row.pedimento(),
        row.partida(),
        row.material(),
        row.rfc(),
        tipo,
        detalle,
        estado,
        "Especialista aduanal",
        "No resuelta",
        now,
        now);
  }

  private String key(ConciliacionRow row) {
    return normalize(row.pedimento()) + "|" + normalize(row.partida()) + "|" + normalize(row.material()) + "|" + normalize(row.rfc());
  }

  private int compare(BigDecimal a, BigDecimal b) {
    return a.compareTo(b);
  }

  private boolean sameDate(LocalDate a, LocalDate b) {
    if (a == null && b == null) return true;
    if (a == null || b == null) return false;
    return a.equals(b);
  }

  private String collectionForSource(String source) {
    String normalized = normalize(source);
    if (normalized.contains("aduan")) return "conciliacion_aduana";
    if (normalized.contains("extern") || normalized.contains("agente")) return "conciliacion_agente";
    return "conciliacion_interno";
  }

  private void ensureCollections() {
    if (!mongoTemplate.collectionExists("conciliacion_interno")) mongoTemplate.createCollection("conciliacion_interno");
    if (!mongoTemplate.collectionExists("conciliacion_aduana")) mongoTemplate.createCollection("conciliacion_aduana");
    if (!mongoTemplate.collectionExists("conciliacion_agente")) mongoTemplate.createCollection("conciliacion_agente");
    if (!mongoTemplate.collectionExists("conciliacion_excepcion")) mongoTemplate.createCollection("conciliacion_excepcion");
  }

  private String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase();
  }

  private String stringValue(Object value) {
    return value == null ? "" : value.toString();
  }

  private BigDecimal bigDecimalValue(Object value) {
    if (value == null) return BigDecimal.ZERO;
    if (value instanceof BigDecimal d) return d;
    if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
    try { return new BigDecimal(value.toString()); } catch (Exception ex) { return BigDecimal.ZERO; }
  }

  private LocalDate localDateValue(Object value) {
    if (value == null) return null;
    if (value instanceof LocalDate d) return d;
    try { return LocalDate.parse(value.toString()); } catch (Exception ex) { return null; }
  }

  private Instant instantValue(Object value) {
    if (value == null) return null;
    if (value instanceof Instant i) return i;
    try { return Instant.parse(value.toString()); } catch (Exception ex) { return null; }
  }
}