package com.mydomain.debug;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesDumpPostProcessor implements EnvironmentPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(PropertiesDumpPostProcessor.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {
        log.info("=== BEGIN ENVIRONMENT DUMP ===");
        for (var ps : env.getPropertySources()) {
            if (ps instanceof EnumerablePropertySource<?> eps) {
                for (String key : eps.getPropertyNames()) {
                    String val = env.getProperty(key);
                    log.info("{} = {}", key, val);
                }
            }
        }
        log.info("=== END ENVIRONMENT DUMP ===");
    }
}
