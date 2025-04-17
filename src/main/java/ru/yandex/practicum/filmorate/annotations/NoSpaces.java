package ru.yandex.practicum.filmorate.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.yandex.practicum.filmorate.validators.NoSpacesValidator;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoSpacesValidator.class)
@Documented
public @interface NoSpaces {

    String message() default "Invalid login";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
