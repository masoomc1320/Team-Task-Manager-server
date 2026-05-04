package com.taskmanager.security;

import com.taskmanager.model.Role;

public record AuthUser(Long id, Role role) {
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
