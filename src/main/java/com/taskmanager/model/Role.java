package com.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
    ADMIN("admin"),
    MEMBER("member");

    private final String jsonValue;

    Role(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String getJsonValue() {
        return jsonValue;
    }

    @JsonCreator
    public static Role fromJson(String raw) {
        if (raw == null || raw.isBlank()) {
            return MEMBER;
        }
        for (Role r : values()) {
            if (r.jsonValue.equalsIgnoreCase(raw) || r.name().equalsIgnoreCase(raw)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + raw);
    }
}
