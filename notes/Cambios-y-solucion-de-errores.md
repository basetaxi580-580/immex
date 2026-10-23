---
type: note
project: IMMEX
topic: cambios-2026-10-21
status: active
created: 2026-10-21
---

# Cambios y solución de errores

## Resumen
- Se consolidó el dashboard principal con métricas de saldo, alertas por vencimiento y tabla por material.
- Se añadió la campana de alertas de 90/60/30 días con deduplicación y sugerencias de acción.
- Se creó la vista de auditor de solo lectura.
- Se agregó la exportación de Anexo 24 en Excel con filtros por razón social y mes.
- Se mantuvo el flujo de importación, PEPS, simulación PEPS, producción mensual, exportación y auditoría.
- Se añadieron casos de datos ficticios realistas para inyección, arneses y doble razón social.

## Errores corregidos
- Se corrigió el backend de `CollectionBootstrap` al usar el material correcto al recalcular valores de lote.
- Se corrigió el mapeo de auditoría para usar la colección `evento_auditoria`.
- Se agregó la dependencia `poi-ooxml` para el Excel.
- Se corrigió la navegación y la ruta de la vista de auditor.
- Se corrigió la inclusión del botón "Descargar reporte".
- Se corrigió la integración del simulador PEPS para exponer simular, aplicar y revertir.
- Se corrigió el error de compilación de `inventory.page.ts` al reintroducir el arreglo `records` que alimenta filtros y listado.

## Verificación final
- Backend en `UP`.
- Frontend en `UP`.
- Dashboard, alertas, auditoría, Anexo 24, simulador PEPS e inventario con casos separados verificados en vivo.
- URL pública: `http://34.69.199.19:4201/`.
