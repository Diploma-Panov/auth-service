package com.mpanov.diploma.auth.model;

public enum UserSystemRole {
    USER,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
