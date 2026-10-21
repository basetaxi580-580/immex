package com.immex.backend.web;

import com.immex.backend.model.CollectionSnapshot;
import java.util.List;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminCollectionsController {
  private final MongoTemplate mongoTemplate;

  public AdminCollectionsController(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @GetMapping("/collections")
  public List<CollectionSnapshot> collections() {
    return List.of(
        snapshot("razon_social"),
        snapshot("pedimento"),
        snapshot("lote_material"),
        snapshot("bom"),
        snapshot("orden_produccion"),
        snapshot("descarga"),
        snapshot("exportacion"),
        snapshot("evento_auditoria"));
  }

  private CollectionSnapshot snapshot(String name) {
    var collection = mongoTemplate.getCollection(name);
    long count = collection.countDocuments();
    List<String> sample = collection.find().limit(2)
        .into(new java.util.ArrayList<Document>())
        .stream()
        .map(doc -> doc.toJson())
        .toList();
    return new CollectionSnapshot(name, count, sample);
  }
}
