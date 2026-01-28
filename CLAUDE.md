# Spring Boot Best Practices - TOUJOURS ACTIF

**IMPORTANT : Applique automatiquement ces règles à TOUT code Spring Boot sans attendre qu'on te le demande.**

## 🎯 Activation Automatique

Dès que tu vois :
- Fichiers `.java` avec `@Entity`, `@Service`, `@RestController`
- Fichiers `application.yml`, `pom.xml`, `build.gradle`
- Mots-clés "spring boot", "jpa", "hibernate"

→ **Active automatiquement les règles Spring Boot Best Practices**

## 📚 Règles (dans .claude-skills/spring-boot-best-practices/rules/)

### CRITIQUE - À TOUJOURS appliquer :

1. **N+1 Queries** (`perf-n-plus-one.md`)
   - ✅ TOUJOURS `fetch = FetchType.LAZY`
   - ✅ TOUJOURS `JOIN FETCH` pour charger relations
   - ❌ JAMAIS de relations EAGER par défaut

2. **Transactions** (`database-transactions.md`)
   - ✅ TOUJOURS `@Transactional(readOnly = true)` sur les services
   - ✅ `@Transactional` pour les écritures

3. **Security Actuator** (`security-actuator.md`)
   - ❌ JAMAIS `include: "*"`
   - ✅ TOUJOURS `include: "health,info"`

4. **SQL Injection** (`security-sql-injection.md`)
   - ✅ TOUJOURS requêtes paramétrées avec `:param`
   - ❌ JAMAIS de concatenation SQL

5. **HikariCP** (`perf-connection-pooling.md`)
   - ✅ TOUJOURS configurer en production

6. **API Errors** (`api-error-handling.md`)
   - ✅ TOUJOURS `@RestControllerAdvice`

7. **Configuration** (`config-externalization.md`)
   - ✅ TOUJOURS `${ENV_VAR}`

8. **Profiles** (`config-profiles.md`)
   - ✅ TOUJOURS `application-{profile}.yml`

9. **Migrations** (`database-migrations.md`)
   - ✅ Flyway/Liquibase obligatoire

10. **Health Checks** (`deploy-health-checks.md`)
    - ✅ TOUJOURS configurer pour Kubernetes

## 🔍 Analyse Automatique

Pour CHAQUE code Spring Boot montré, vérifie automatiquement et signale :
- ⚠️ N+1 queries potentielles
- ⚠️ Transactions manquantes
- ❌ Problèmes de sécurité
- 💡 Optimisations possibles

**Format obligatoire :**
```
🔍 Analyse Spring Boot Best Practices :

❌ CRITIQUE :
- [fichier:ligne] Relation EAGER détectée → N+1 queries
- [fichier:ligne] Actuator exposé avec include: "*"

⚠️ À améliorer :
- [fichier:ligne] Ajouter @Transactional

✅ Conforme :
- Configuration externalisée
- Requêtes paramétrées
```

## 💻 Génération de Code

Génère TOUJOURS du code conforme. Exemple :

```java
@Entity
@Table(name = "users", indexes = {@Index(name = "idx_email", columnList = "email")})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)  // ✅ LAZY explicite
    private List<Order> orders = new ArrayList<>();
    
    // ✅ equals/hashCode sur ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return id != null && id.equals(((User) o).id);
    }
}
```

Indique les règles appliquées :
```
📋 Règles appliquées :
- ✅ perf-n-plus-one.md - LAZY + Collection initialisée
- ✅ database-relationships.md - equals/hashCode sur ID
```

**Ces règles sont OBLIGATOIRES et AUTOMATIQUES. Tu ne demandes JAMAIS si tu dois les appliquer.**
