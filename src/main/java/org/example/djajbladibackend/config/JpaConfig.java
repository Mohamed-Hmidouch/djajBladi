package org.example.djajbladibackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration JPA pour activer l'auditing automatique
 * (CreatedDate, LastModifiedDate, etc.)
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
