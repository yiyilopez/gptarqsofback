# Chat Backend (Spring Boot, Gradle, Hexagonal)

## Requisitos
- Java 17
- Gradle
- Ollama instalado y modelo descargado (`ollama pull llama3.1:8b`)

## Correr
```sh
./gradlew bootRun
```

## Probar
```sh
curl -H "Authorization: Bearer demo-token" \
     -H "Content-Type: application/json" \
     -d '{ "messages":[{"role":"user","content":"Explícame SOLID en 3 líneas."}] }' \
     http://localhost:8080/v1/chat
```

Para stream (SSE):
```sh
curl -H "Authorization: Bearer demo-token" \
     http://localhost:8080/v1/chat/stream?cid=...
```

## Endpoints
- `POST /v1/chat` → inicia/actualiza conversación (no streaming)
- `GET /v1/chat/stream?cid=...` → SSE con respuesta fragmentada

## Seguridad
- Auth por token (header Authorization: Bearer ...)
- Rate limit por token (30 req/min)
- CORS: permite http://localhost:3000

## Arquitectura
- Hexagonal: domain, application, infrastructure (controller, adapter, security, config)
- Sin BD: usa ConcurrentHashMap para historial y tokens
- Proveedor LLM: Ollama local vía HTTP

## Extras
- Moderación simple (palabras prohibidas)
- Healthcheck: `/actuator/health`

---

Entrega: código completo, comentarios breves, listo para correr y probar con frontend.
