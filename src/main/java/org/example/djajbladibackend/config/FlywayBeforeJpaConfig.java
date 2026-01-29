package org.example.djajbladibackend.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import java.util.Arrays;

/**
 * Ensures EntityManagerFactory depends on Flyway so migrations run before JPA validates schema.
 * Fixes "relation \"users\" does not exist" in prod (e.g. Koyeb) when Flyway order was wrong.
 */
@Configuration
public class FlywayBeforeJpaConfig implements org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor, PriorityOrdered {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        for (String name : registry.getBeanDefinitionNames()) {
            if ("entityManagerFactory".equals(name)) {
                BeanDefinition bd = registry.getBeanDefinition(name);
                bd.setDependsOn(append(bd.getDependsOn(), "flyway"));
                return;
            }
        }
    }

    @Override
    public void postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory beanFactory) {
        // no-op
    }

    private static String[] append(String[] existing, String extra) {
        if (existing == null || existing.length == 0) {
            return new String[] { extra };
        }
        if (Arrays.asList(existing).contains(extra)) {
            return existing;
        }
        String[] out = Arrays.copyOf(existing, existing.length + 1);
        out[existing.length] = extra;
        return out;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
