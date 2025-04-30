package com.mpanov.diploma.auth;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PropertiesLogger implements ApplicationListener<ApplicationReadyEvent> {

    private final ConfigurableEnvironment env;

    public PropertiesLogger(ConfigurableEnvironment env) {
        this.env = env;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("=== DUMPING ALL PROPERTIES ===");
        for (PropertySource<?> ps : env.getPropertySources()) {
            if (ps instanceof EnumerablePropertySource<?>) {
                for (String key : ((EnumerablePropertySource<?>) ps).getPropertyNames()) {
                    String val = env.getProperty(key);
                    log.info("{} -> {}", key, val);
                }
            }
        }
        log.info("=== END PROPERTY DUMP ===");
    }
}
