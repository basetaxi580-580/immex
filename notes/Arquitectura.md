---
type: note
project: IMMEX
topic: arquitectura
status: draft
created: 2026-10-20
---

# Arquitectura IMMEX

## Objetivo
Documentar la arquitectura del sistema IMMEX como fuente de verdad del proyecto.

## Componentes
- Frontend: `frontend/`
- Backend: `backend/`
- Base de datos: MongoDB
- Orquestación local: `docker-compose.yml`

## Separación de proyectos
- IMMEX es independiente de Base580.
- No comparte Docker, instancia ni despliegue con Base580.
- La documentación y la memoria de IMMEX viven en este vault.

## Temas documentados
- Flujo de arranque local
- Variables de entorno
- Puertos expuestos
- Modelo de datos
- Endpoints principales
- Estrategia de despliegue
- Dashboard operativo con alertas, auditoría y reporte Anexo 24
- Vista de auditor en solo lectura
- Simulador PEPS con aplicar/revertir y bitácora
- Casos de datos ficticios para inyección, arneses y doble razón social
