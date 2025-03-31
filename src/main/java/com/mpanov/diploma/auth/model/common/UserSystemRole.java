package com.mpanov.diploma.auth.model.common;

public enum UserSystemRole {
    USER,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
