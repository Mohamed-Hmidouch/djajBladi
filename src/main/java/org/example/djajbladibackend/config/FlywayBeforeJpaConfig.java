package org.example.djajbladibackend.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import java.util.Arrays;

/**
 * When Flyway is present, ensures EntityManagerFactory depends on it so migrations run
 * before JPA validates schema. If Flyway bean is not registered (e.g. disabled), we do
 * nothing so startup is not blocked.
 */
@Configuration(proxyBeanMethods = false)
public class FlywayBeforeJpaConfig {

    @Bean
    public static BeanDefinitionRegistryPostProcessor flywayBeforeJpaPostProcessor() {
        return new FlywayBeforeJpaPostProcessor();
    }

    private static final class FlywayBeforeJpaPostProcessor implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

        private static final String[] EMF_NAMES = { "entityManagerFactory", "jpaSharedEM_entityManagerFactory" };

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
            if (!registry.containsBeanDefinition("flyway")) {
                return;
            }
            for (String emfName : EMF_NAMES) {
                if (registry.containsBeanDefinition(emfName)) {
                    BeanDefinition bd = registry.getBeanDefinition(emfName);
                    bd.setDependsOn(append(bd.getDependsOn(), "flyway"));
                    return;
                }
            }
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
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
}
