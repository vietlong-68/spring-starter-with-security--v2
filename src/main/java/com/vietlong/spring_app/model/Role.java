package com.vietlong.spring_app.model;

import com.vietlong.spring_app.exception.AppException;
import com.vietlong.spring_app.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum Role {
    USER("Người dùng", "Quyền cơ bản của người dùng"),
    ADMIN("Quản trị viên", "Quyền quản trị hệ thống");

    private final String displayName;
    private final String description;

    Role(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isUser() {
        return this == USER;
    }

    public static Role fromString(String roleString) throws AppException {
        if (roleString == null || roleString.trim().isEmpty()) {
            return USER;
        }

        try {
            return Role.valueOf(roleString.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Invalid role: " + roleString + ". Valid roles are: " + String.join(", ", getRoleNames()));
        }
    }

    public static String[] getRoleNames() {
        Role[] roles = Role.values();
        String[] roleNames = new String[roles.length];
        for (int i = 0; i < roles.length; i++) {
            roleNames[i] = roles[i].name();
        }
        return roleNames;
    }
}
