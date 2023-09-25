# REST API

Simple HTTP server (ServerSocket) on port 8080. In-memory user store. Manual JSON building, no external libs.

**Author:** Bisman Singh <bismanmadaan1@gmail.com>

## Endpoints

- `GET /api/users` - Return JSON array of all users
- `POST /api/users` - Create user, body: `{"name":"X","email":"Y"}`
- `GET /api/users/:id` - Get user by ID

## Build & Run

```bash
make
make run
```

## Example

```bash
curl -X POST http://localhost:8080/api/users -d '{"name":"Alice","email":"alice@example.com"}'
curl http://localhost:8080/api/users
curl http://localhost:8080/api/users/1
```
