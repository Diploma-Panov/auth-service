package com.mpanov.diploma.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;

public class PropertiesDumpPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {
        System.err.println("=== BEGIN ENVIRONMENT DUMP ===");
        for (var ps : env.getPropertySources()) {
            if (ps instanceof EnumerablePropertySource<?> eps) {
                for (String key : eps.getPropertyNames()) {
                    String val = env.getProperty(key);
                    System.err.println(key + " = " + val);
                }
            }
        }
        System.err.println("=== END ENVIRONMENT DUMP ===");
    }
}
