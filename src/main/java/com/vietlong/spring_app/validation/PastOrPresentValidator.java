package com.vietlong.spring_app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class PastOrPresentValidator implements ConstraintValidator<PastOrPresent, LocalDate> {

    @Override
    public void initialize(PastOrPresent constraintAnnotation) {
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        LocalDate today = LocalDate.now();
        return !value.isAfter(today);
    }
}
