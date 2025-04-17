package ru.yandex.practicum.filmorate.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.annotations.MinReleaseDate;

import java.time.LocalDate;
import java.time.Month;

public class MinReleaseDateValidator implements ConstraintValidator<MinReleaseDate, LocalDate> {

    private final LocalDate minReleaseDate = LocalDate.of(1895, Month.DECEMBER, 28);

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) return true;
        return date.isAfter(minReleaseDate);
    }
}
