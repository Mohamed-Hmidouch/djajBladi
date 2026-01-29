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
        String password = env.getProperty("FLYWAY_PASSWORD");
        final String url = flywayUrl;
        return config -> {
            if (StringUtils.hasText(url)) {
                config.dataSource(
                    url,
                    StringUtils.hasText(user) ? user : "",
                    StringUtils.hasText(password) ? password : ""
                );
            }
        };
    }
}
