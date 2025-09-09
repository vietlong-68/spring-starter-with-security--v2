package com.vietlong.spring_app.model;

import com.vietlong.spring_app.exception.AppException;
import com.vietlong.spring_app.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum Gender {
    MALE("Nam"),
    FEMALE("Nữ"),
    OTHER("Khác");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public static Gender fromString(String genderString) throws AppException {
        if (genderString == null || genderString.trim().isEmpty()) {
            return null;
        }

        try {
            return Gender.valueOf(genderString.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Invalid gender: " + genderString + ". Valid genders are: MALE, FEMALE, OTHER");
        }
    }
}
