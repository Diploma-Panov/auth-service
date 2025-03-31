package com.mpanov.diploma.auth.exception.common;

public class NotFoundException extends RuntimeException {
    public NotFoundException(Class<?> entityClass, String field, String value) {
        super(
            "%s with %s='%s' not found".formatted(
                entityClass.getSimpleName(), field, value
            )
        );
    }
}
