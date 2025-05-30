package ru.yandex.practicum.filmorate.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.annotations.Marker;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FilmTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private Film film;

    @BeforeEach
    void createFilm() {
        film = new Film(1,"test Title", "Test Description", LocalDate.now(), 100);
    }

    @Test
    void validateJsonFormat() {
        final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();

        final String uncorrectedDateJson = "{\"releaseDate\":\"1-12-12\"}";

        assertThrows(DateTimeParseException.class, () -> {
            gson.fromJson(uncorrectedDateJson, Film.class);
        });

        final String uncorrectedIdJson = "{\"id\":\"id100\"}";

        assertThrows(JsonSyntaxException.class, () -> {
            gson.fromJson(uncorrectedIdJson, Film.class);
        });
    }

    @Test
    void validateOnCreateAndOnUpdateFilm() {
        Set<ConstraintViolation<Film>> constraintViolations = validator.validate(film);
        assertThat(constraintViolations).hasSize(0);

        final char[] data = new char[201];
        film.setDescription(new String(data));
        constraintViolations = validator.validate(film);
        assertThat(constraintViolations).hasSize(1);

        assertThat(constraintViolations).extracting(ConstraintViolation::getMessage).containsExactlyInAnyOrder(
                "максимальная длина описания - 200 символов"
        );
    }

    @Test
    void validateOnCreateFilm() {
        Set<ConstraintViolation<Film>> constraintViolations = validator.validate(film, Marker.OnCreate.class);
        assertThat(constraintViolations).hasSize(0);

        film.setName("  ");
        constraintViolations = validator.validate(film, Marker.OnCreate.class);
        assertThat(constraintViolations).hasSize(1);

        film.setDescription("  ");
        constraintViolations = validator.validate(film, Marker.OnCreate.class);
        assertThat(constraintViolations).hasSize(2);

        film.setReleaseDate(LocalDate.MIN);
        constraintViolations = validator.validate(film, Marker.OnCreate.class);
        assertThat(constraintViolations).hasSize(3);

        film.setDuration(0);
        constraintViolations = validator.validate(film, Marker.OnCreate.class);
        assertThat(constraintViolations).hasSize(4);

        film.setId(-100); // id не проверяется OnCreate
        constraintViolations = validator.validate(film, Marker.OnCreate.class);
        assertThat(constraintViolations).hasSize(4);

        assertThat(constraintViolations).extracting(ConstraintViolation::getMessage).containsExactlyInAnyOrder(
                "имя не должно быть пустым",
                "описание не должно быть пустым",
                "дата должна быть не ранее 28.12.1985",
                "продолжительность должна быть больше 0"
        );
    }

    @Test
    void validateOnUpdateFilm() {
        Set<ConstraintViolation<Film>> constraintViolations = validator.validate(film, Marker.OnUpdate.class);
        assertThat(constraintViolations).hasSize(0);

        film.setId(0);
        constraintViolations = validator.validate(film, Marker.OnUpdate.class);
        assertThat(constraintViolations).hasSize(1);

        film.setReleaseDate(LocalDate.MIN);
        constraintViolations = validator.validate(film, Marker.OnUpdate.class);
        assertThat(constraintViolations).hasSize(2);

        film.setName(null); // Name не проверяется на null OnUpdate
        constraintViolations = validator.validate(film, Marker.OnUpdate.class);
        assertThat(constraintViolations).hasSize(2);

        assertThat(constraintViolations).extracting(ConstraintViolation::getMessage).containsExactlyInAnyOrder(
                "id должен быть указан и быть больше нуля",
                "дата должна быть не ранее 28.12.1985"
        );
    }
}