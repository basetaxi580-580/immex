package com.immex.backend.service;

import com.immex.backend.model.Bom;
import com.immex.backend.model.Descarga;
import com.immex.backend.model.EventoAuditoria;
import com.immex.backend.model.Exportacion;
import com.immex.backend.model.LoteMaterial;
import com.immex.backend.model.OrdenProduccion;
import com.immex.backend.model.Pedimento;
import com.immex.backend.model.RazonSocial;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class CollectionBootstrap implements ApplicationRunner {
  private final MongoTemplate mongoTemplate;

  public CollectionBootstrap(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public void run(ApplicationArguments args) {
    ensure(RazonSocial.class, "razon_social");
    ensure(Pedimento.class, "pedimento");
    ensure(LoteMaterial.class, "lote_material");
    ensure(Bom.class, "bom");
    ensure(OrdenProduccion.class, "orden_produccion");
    ensure(Descarga.class, "descarga");
    ensure(Exportacion.class, "exportacion");
    ensure(EventoAuditoria.class, "evento_auditoria");
  }

  private void ensure(Class<?> type, String collectionName) {
    if (!mongoTemplate.collectionExists(collectionName)) {
      mongoTemplate.createCollection(collectionName);
    }
  }
}
