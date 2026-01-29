# djajBladi

## Run with Docker (one instance, no external DB)

App + Postgres + Redis run together in one compose stack. The app uses the **postgres** and **redis** services from the same compose—no `DATABASE_URL` or Redis URL to configure.

```bash
# Build and run app, Postgres, Redis
docker compose up -d --build

# API: http://localhost:8081
# Optional pgadmin: docker compose --profile admin up -d
```

Set `JWT_SECRET` in production (e.g. `openssl rand -hex 64`):

```bash
export JWT_SECRET=your-secret-here
docker compose up -d --build
```

For production, use strong passwords and consider removing `ports` for `postgres` and `redis` in `compose.yaml` so only the app (8081) is exposed.

## Security

- **JWT**: Always set `JWT_SECRET` in production (`openssl rand -hex 64`). No default production secret.
- **Actuator**: Only `health` and `info` exposed (never `*`).
- **Vulnerabilities**: Run `mvn dependency-check:check` before releases.
- **Test scripts**: Use `test-*.sh.example` with `TEST_LOGIN_EMAIL` / `TEST_LOGIN_PASSWORD`; never commit credentials.

## Redis caching

Responses are cached in Redis: first request hits the backend; subsequent requests with the same key are served from cache.

- **Cached**: user by email, email-exists check, `GET /api/users/me`.
- **TTL**: 10 minutes (`spring.cache.redis.time-to-live`).
- **Eviction**: on register, cache entries for the new email are evicted.
