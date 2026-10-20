---
type: note
project: IMMEX
topic: arranque-local
status: draft
created: 2026-10-20
---

# Arranque local IMMEX

## Comandos
```bash
docker compose up --build
```

## Servicios
- Frontend: `http://localhost:4201`
- Backend: `http://localhost:8081`
- MongoDB: interno por Docker

## Variables de entorno del backend
- `SERVER_PORT=8080`
- `MONGODB_URI=mongodb://mongo:27017/immex`

## Notas
- El backend usa `spring.data.mongodb.uri` con fallback a `mongodb://localhost:27017/immex`.
- En Docker Compose se sobreescribe a `mongodb://mongo:27017/immex`.
- El frontend consume `/api/records` y depende del backend expuesto por el mismo origen o proxy.
