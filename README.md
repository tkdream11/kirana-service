# Auth Service (Spring Boot 4.0.3 + Java 25 Virtual Threads + JWT Access/Refresh Tokens)

A Spring Boot authentication service using Java 25 virtual threads for efficient concurrency, with JWT access/refresh token flows.

## Stack
- Java 25
- Spring Boot 4.0.3
- Spring Security
- Gradle
- JWT (`jjwt`)

## Features
- User registration and login APIs
- BCrypt password hashing
- JWT access token + refresh token issuance
- Configurable expirations:
  - Access token: 15 minutes (default, configurable)
  - Refresh token: 7 days (default, configurable)
- Access token auto-regeneration when request has expired access token + valid `X-Refresh-Token`
- Manual refresh endpoint: `POST /api/auth/refresh`
- Protected endpoint: `GET /api/me`
- Virtual threads enabled (`spring.threads.virtual.enabled=true`)

## Package structure
- `com.demo.app` -> application entry point
- `com.demo.app.auth` -> authentication module (controller/service/security/dto/model)

This keeps auth isolated so additional monolith modules can be added later under `com.demo.app.<module>`.


## Configuration
`src/main/resources/application.properties`:
- `spring.threads.virtual.enabled=true`
- `security.jwt.access-token-expiration-seconds=900`
- `security.jwt.refresh-token-expiration-seconds=604800`

## Run
```bash
./gradlew run
```

## API
### Register
`POST /api/auth/register`
```json
{
  "email": "user@example.com",
  "password": "password123",
  "fullName": "Test User"
}
```

### Login
`POST /api/auth/login`
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

### Refresh tokens
`POST /api/auth/refresh`
```json
{
  "refreshToken": "<refresh-token>"
}
```

### Protected endpoint
`GET /api/me` with header:
- `Authorization: Bearer <access-token>`

Optional auto-regeneration headers:
- `Authorization: Bearer <possibly-expired-access-token>`
- `X-Refresh-Token: <valid-refresh-token>`

If regenerated, server responds with a new access token in response header:
- `Authorization: Bearer <new-access-token>`
