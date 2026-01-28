# Spring Boot Best Practices - Auto-Applied

Automatically apply these rules to ALL Spring Boot code:

## Critical Rules (Always)

1. **JPA Relations**: ALWAYS `fetch = FetchType.LAZY`
2. **Repositories**: ALWAYS `JOIN FETCH` to load relations
3. **Services**: ALWAYS `@Transactional(readOnly = true)`
4. **Security**: NEVER `include: "*"` for Actuator
5. **SQL**: ALWAYS parameterized queries (`:param`)
6. **Config**: ALWAYS externalized (`${ENV_VAR}`)
7. **API**: ALWAYS `@RestControllerAdvice`
8. **HikariCP**: ALWAYS configured in production
9. **Migrations**: ALWAYS Flyway/Liquibase
10. **Health**: ALWAYS Kubernetes probes

Full rules: `.claude-skills/spring-boot-best-practices/rules/`

**Apply automatically without asking.**
