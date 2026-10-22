---
type: note
project: IMMEX
topic: modelo-de-datos
status: draft
created: 2026-10-20
---

# Modelo de datos IMMEX

## Entidades detectadas
- `ImmexRecord`
- `ImmexRecordRequest`
- `ImmexCloseReport`
- `ImmexDashboardSummary`
- `PepsConsumptionRequest`
- `PepsOperationResult`
- `AuditEvent`

## Módulos de negocio
- Registro de materiales
- Consumo PEPS
- Cierre y reporte
- Auditoría
- Resumen operativo

## Pendiente de documentar
- Campos exactos de cada entidad
- Validaciones
- Relaciones lógicas
- Índices o colecciones de MongoDB

## Estado actual ampliado
- `lote_material` guarda saldo, fecha de entrada, fecha de vencimiento, tipo de material, razón social y valor unitario.
- `descarga` registra cada movimiento PEPS con saldo antes/después.
- `evento_auditoria` guarda la bitácora de solo agregar con quién, cuándo, qué y por qué.
- `exportacion` y `pedimento` soportan el flujo de exportaciones y capturas de importación.
- `AuditEvent` se usa como modelo de bitácora y se mapea a `evento_auditoria`.
- `razon_social` permite separar empresas y validar vista consolidada.
- `bom` y `orden_produccion` soportan producción mensual con múltiples componentes.
- `immex_records` conserva el ledger principal y la vista de riesgo.
- `CollectionBootstrap` y `DataSeeder` cargan escenarios ficticios realistas para pruebas de UI y negocio.
