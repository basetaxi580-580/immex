---
type: note
project: IMMEX
topic: stack-tecnico
status: draft
created: 2026-10-20
---

# Stack técnico IMMEX

## Backend
- Spring Boot `3.4.2`
- Java `21`
- MongoDB
- Actuator habilitado
- Validación con `spring-boot-starter-validation`

## Frontend
- Angular `22`
- TypeScript `6`
- HttpClient configurado en `app.config.ts`

## Integración
- Frontend llama endpoints relativos `/api/*`
- Backend expone API REST sobre `server.port`
- Docker Compose une frontend, backend y MongoDB
