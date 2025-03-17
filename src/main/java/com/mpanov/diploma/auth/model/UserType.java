package com.mpanov.diploma.auth.model;

public enum UserType {
    USER,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
