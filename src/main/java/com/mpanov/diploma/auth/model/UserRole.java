package com.mpanov.diploma.auth.model;

public enum UserRole {
    USER, ADMIN;

    @Override
    public String toString() {
        return "ROLE_" + this.name();
    }
}
