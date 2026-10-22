package com.immex.backend.web;

import com.immex.backend.model.RazonSocialOption;
import java.util.List;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/razones-sociales")
public class RazonSocialController {
  private final MongoTemplate mongoTemplate;

  public RazonSocialController(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @GetMapping
  public List<RazonSocialOption> all() {
    if (!mongoTemplate.collectionExists("razon_social")) {
      return List.of();
    }
    return mongoTemplate.getCollection("razon_social").find()
        .into(new java.util.ArrayList<Document>())
        .stream()
        .filter(doc -> doc.getBoolean("activo", true))
        .sorted((a, b) -> stringValue(a.get("nombre")).compareToIgnoreCase(stringValue(b.get("nombre"))))
        .map(doc -> new RazonSocialOption(
            stringValue(doc.get("nombre")),
            stringValue(doc.get("rfc")),
            stringValue(doc.get("programaImmex")),
            doc.getBoolean("activo", true)))
        .toList();
  }

  private String stringValue(Object value) {
    return value == null ? "" : value.toString();
  }
}
