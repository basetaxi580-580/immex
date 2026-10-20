---
type: note
project: IMMEX
topic: deploy
status: active
created: 2026-10-20
---

# Deploy IMMEX

## Estado actual
- Deploy activo en GCP.
- VM: `immex-vm`
- IP pública: `34.69.199.19`

## URLs públicas
- App: `http://34.69.199.19:4201/`
- Swagger UI: `http://34.69.199.19:4201/swagger-ui/index.html`
- OpenAPI: `http://34.69.199.19:4201/v3/api-docs`

## Verificación
- Frontend responde HTTP 200.
- Swagger responde HTTP 200.
- Backend responde detrás del frontend por proxy.

## Separación
- IMMEX no comparte instancia ni Docker con Base580.
- IMMEX vive en su propia VM y su propio stack.
