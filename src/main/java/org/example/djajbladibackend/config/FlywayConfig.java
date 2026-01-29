package org.example.djajbladibackend.config;

import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * Flyway uses a direct DB connection when using Neon. Neon's pooler (PgBouncer) breaks
 * migrations (advisory locks, DDL). Use FLYWAY_URL for direct, or we derive direct from
 * a pooler DATABASE_URL / SPRING_DATASOURCE_URL when FLYWAY_URL is not set.
 * User/password: FLYWAY_USER/FLYWAY_PASSWORD, else SPRING_DATASOURCE_USERNAME/PASSWORD
 * or DATABASE_USERNAME/PASSWORD. Ensure these are set when overriding Flyway URL.
 * See https://neon.tech/docs/connect/connection-pooling
 */
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayConfigurationCustomizer flywayUrlCustomizer(Environment env) {
        String flywayUrl = env.getProperty("FLYWAY_URL");
        if (!StringUtils.hasText(flywayUrl)) {
            String dsUrl = env.getProperty("SPRING_DATASOURCE_URL");
            if (!StringUtils.hasText(dsUrl)) dsUrl = env.getProperty("DATABASE_URL");
            if (StringUtils.hasText(dsUrl) && dsUrl.contains("-pooler")) {
                flywayUrl = dsUrl.replace("-pooler.", ".");
            }
        }
        String user = env.getProperty("FLYWAY_USER");
        if (!StringUtils.hasText(user)) user = env.getProperty("SPRING_DATASOURCE_USERNAME");
        if (!StringUtils.hasText(user)) user = env.getProperty("DATABASE_USERNAME");
        String password = env.getProperty("FLYWAY_PASSWORD");
        if (!StringUtils.hasText(password)) password = env.getProperty("SPRING_DATASOURCE_PASSWORD");
        if (!StringUtils.hasText(password)) password = env.getProperty("DATABASE_PASSWORD");
        final String url = flywayUrl;
        final String u = user != null ? user : "";
        final String p = password != null ? password : "";
        return config -> {
            if (StringUtils.hasText(url)) {
                config.dataSource(url, u, p);
            }
        };
    }
}
