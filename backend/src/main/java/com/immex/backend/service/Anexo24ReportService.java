package com.immex.backend.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class Anexo24ReportService {
  private static final DateTimeFormatter MONTH_FILENAME = DateTimeFormatter.ofPattern("yyyy-MM");
  private final MongoTemplate mongoTemplate;

  public Anexo24ReportService(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public byte[] generate(String razonSocialFilter, String mesFilter) {
    ensureCollectionExists();
    String razonSocial = normalizeFilter(razonSocialFilter);
    YearMonth mes = parseMonth(mesFilter);

    List<Document> lotes = new ArrayList<>(mongoTemplate.getCollection("lote_material").find().into(new ArrayList<>()));
    Map<String, Long> descargasPorLote = cargarDescargasPorLote();
    List<Anexo24Row> rows = new ArrayList<>();

    for (Document lote : lotes) {
      LocalDate fechaEntrada = readDate(lote.get("fechaEntrada"));
      if (fechaEntrada == null) {
        continue;
      }
      if (mes != null && !YearMonth.from(fechaEntrada).equals(mes)) {
        continue;
      }

      String loteRazonSocial = normalize(stringValue(lote.get("razonSocial")));
      if (!razonSocial.isBlank() && !loteRazonSocial.contains(razonSocial)) {
        continue;
      }

      String loteId = objectIdString(lote.get("_id"));
      String pedimento = stringValue(lote.get("pedimento"));
      long descargas = descargasPorLote.getOrDefault(loteId, 0L);
      long saldo = longValue(lote.get("saldo"));
      long cantidadImportada = longValue(lote.get("saldoInicial"));
      if (cantidadImportada <= 0) {
        cantidadImportada = saldo + descargas;
      }
      LocalDate vencimiento = readDate(lote.get("fechaVencimiento"));
      rows.add(new Anexo24Row(
          stringValue(lote.get("razonSocial")),
          pedimento,
          fechaEntrada,
          cantidadImportada,
          descargas,
          saldo,
          vencimiento));
    }

    rows.sort(Comparator.comparing(Anexo24Row::fechaEntrada).thenComparing(Anexo24Row::pedimento));

    try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Anexo 24");
      CreationHelper creationHelper = workbook.getCreationHelper();
      CellStyle headerStyle = headerStyle(workbook);
      CellStyle dateStyle = dateStyle(workbook, creationHelper);
      CellStyle integerStyle = integerStyle(workbook);
      CellStyle moneyStyle = moneyStyle(workbook);

      int rowIndex = 0;
      Row title = sheet.createRow(rowIndex++);
      title.createCell(0).setCellValue("Reporte Anexo 24");
      Row meta = sheet.createRow(rowIndex++);
      meta.createCell(0).setCellValue("Filtro razón social: " + (razonSocial.isBlank() ? "Todas" : razonSocialFilter.trim()));
      meta.createCell(3).setCellValue("Filtro mes: " + (mes == null ? "Todos" : mes.toString()));
      rowIndex++;

      Row header = sheet.createRow(rowIndex++);
      String[] headers = {"Razón social", "Pedimento", "Fecha de entrada", "Cantidad importada", "Descargas", "Saldo", "Vencimiento"};
      for (int i = 0; i < headers.length; i++) {
        Cell cell = header.createCell(i);
        cell.setCellValue(headers[i]);
        cell.setCellStyle(headerStyle);
      }

      for (Anexo24Row item : rows) {
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(item.razonSocial());
        row.createCell(1).setCellValue(item.pedimento());
        Cell fechaEntradaCell = row.createCell(2);
        fechaEntradaCell.setCellValue(item.fechaEntrada());
        fechaEntradaCell.setCellStyle(dateStyle);
        Cell importadaCell = row.createCell(3);
        importadaCell.setCellValue(item.cantidadImportada());
        importadaCell.setCellStyle(integerStyle);
        Cell descargasCell = row.createCell(4);
        descargasCell.setCellValue(item.descargas());
        descargasCell.setCellStyle(integerStyle);
        Cell saldoCell = row.createCell(5);
        saldoCell.setCellValue(item.saldo());
        saldoCell.setCellStyle(integerStyle);
        Cell vencimientoCell = row.createCell(6);
        if (item.vencimiento() != null) {
          vencimientoCell.setCellValue(item.vencimiento());
          vencimientoCell.setCellStyle(dateStyle);
        }
      }

      for (int i = 0; i < headers.length; i++) {
        sheet.autoSizeColumn(i);
      }
      workbook.write(output);
      return output.toByteArray();
    } catch (IOException ex) {
      throw new IllegalStateException("No se pudo generar el reporte Anexo 24", ex);
    }
  }

  private Map<String, Long> cargarDescargasPorLote() {
    List<Document> descargas = mongoTemplate.getCollection("descarga").find().into(new ArrayList<>());
    return descargas.stream()
        .filter(doc -> doc.get("cantidad") != null)
        .collect(Collectors.groupingBy(doc -> resolveLoteKey(doc), Collectors.summingLong(doc -> longValue(doc.get("cantidad")))));
  }

  private String resolveLoteKey(Document descarga) {
    String loteId = objectIdString(descarga.get("loteId"));
    if (!loteId.isBlank()) {
      return loteId;
    }
    return normalize(stringValue(descarga.get("material"))) + "|" + normalize(stringValue(descarga.get("pedimento")));
  }

  private void ensureCollectionExists() {
    if (!mongoTemplate.collectionExists("lote_material")) {
      mongoTemplate.createCollection("lote_material");
    }
    if (!mongoTemplate.collectionExists("descarga")) {
      mongoTemplate.createCollection("descarga");
    }
  }

  private CellStyle headerStyle(XSSFWorkbook workbook) {
    Font font = workbook.createFont();
    font.setBold(true);
    font.setColor(IndexedColors.WHITE.getIndex());
    CellStyle style = workbook.createCellStyle();
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    return style;
  }

  private CellStyle dateStyle(XSSFWorkbook workbook, CreationHelper helper) {
    CellStyle style = workbook.createCellStyle();
    style.setDataFormat(helper.createDataFormat().getFormat("dd/mm/yyyy"));
    return style;
  }

  private CellStyle integerStyle(XSSFWorkbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setDataFormat(workbook.createDataFormat().getFormat("0"));
    return style;
  }

  private CellStyle moneyStyle(XSSFWorkbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
    return style;
  }

  private String normalizeFilter(String value) {
    return normalize(value == null ? "" : value);
  }

  private String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
  }

  private String stringValue(Object value) {
    return value == null ? "" : value.toString();
  }

  private String objectIdString(Object value) {
    if (value instanceof ObjectId objectId) {
      return objectId.toHexString();
    }
    return value == null ? "" : value.toString();
  }

  private long longValue(Object value) {
    if (value == null) return 0L;
    if (value instanceof Number number) return number.longValue();
    try {
      return Long.parseLong(value.toString());
    } catch (NumberFormatException ex) {
      return 0L;
    }
  }

  private LocalDate readDate(Object value) {
    if (value == null) return null;
    if (value instanceof LocalDate localDate) return localDate;
    return LocalDate.parse(value.toString());
  }

  private YearMonth parseMonth(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return YearMonth.parse(value.trim());
    } catch (Exception ex) {
      return null;
    }
  }

  private record Anexo24Row(String razonSocial, String pedimento, LocalDate fechaEntrada, long cantidadImportada, long descargas, long saldo, LocalDate vencimiento) {}
}
