package com.vietlong.spring_app.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PastOrPresentValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PastOrPresent {
    String message() default "Ngày sinh không được ở trong tương lai";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
