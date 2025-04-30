package com.mpanov.diploma.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;

@Slf4j
public class PropertiesDumpPostProcessor implements EnvironmentPostProcessor {

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
