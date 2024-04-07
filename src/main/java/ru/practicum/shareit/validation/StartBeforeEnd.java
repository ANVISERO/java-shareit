package ru.practicum.shareit.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = BookingTimeValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StartBeforeEnd {
    String message() default "{ru.yandex.practicum.filmorate.validation.StartBeforeEnd.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
