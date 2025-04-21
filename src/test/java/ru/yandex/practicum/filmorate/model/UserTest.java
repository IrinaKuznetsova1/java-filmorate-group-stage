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

class UserTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private User user;

    @BeforeEach
    void createUser() {
        user = new User(1,"test@mail.com", "TestLogin", "Test Name", LocalDate.MIN);
    }

    @Test
    void validateJsonFormat() {
        final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();

        final String uncorrectedDateJson = "{\"birthday\":\"1-12-12\"}";

        assertThrows(DateTimeParseException.class, () -> {
            gson.fromJson(uncorrectedDateJson, User.class);
        });

        final String uncorrectedIdJson = "{\"id\":\"id100\"}";

        assertThrows(JsonSyntaxException.class, () -> {
            gson.fromJson(uncorrectedIdJson, User.class);
        });
    }

    @Test
    void validateOnCreateAndOnUpdateUser() {
        Set<ConstraintViolation<User>> constraintViolations = validator.validate(user);
        assertThat(constraintViolations).hasSize(0);

        user.setEmail("mail");
        constraintViolations = validator.validate(user);
        assertThat(constraintViolations).hasSize(1);

        user.setLogin("LoginWithTab\t");
        constraintViolations = validator.validate(user);
        assertThat(constraintViolations).hasSize(2);

        user.setBirthday(LocalDate.MAX);
        constraintViolations = validator.validate(user);
        assertThat(constraintViolations).hasSize(3);

        assertThat(constraintViolations).extracting(ConstraintViolation::getMessage).containsExactlyInAnyOrder(
                "строка должна соответствовать формату адреса электронной почты",
                "логин не должен содержать пробелы, переносы строки или табуляцию",
                "дата должна быть прошедшей"
        );
    }

    @Test
    void validateOnCreateUser() {
        Set<ConstraintViolation<User>> constraintViolations = validator.validate(user, Marker.OnCreate.class);
        assertThat(constraintViolations).hasSize(0);

        user.setEmail(" ");
        constraintViolations = validator.validate(user, Marker.OnCreate.class);
        assertThat(constraintViolations).hasSize(1);

        user.setLogin("  ");
        constraintViolations = validator.validate(user, Marker.OnCreate.class);
        assertThat(constraintViolations).hasSize(2);

        user.setBirthday(LocalDate.MAX); // Birthday не должна проверяться OnCreate
        constraintViolations = validator.validate(user, Marker.OnCreate.class);
        assertThat(constraintViolations).hasSize(2);

        assertThat(constraintViolations).extracting(ConstraintViolation::getMessage).containsExactlyInAnyOrder(
                "e-mail не должен быть null или быть пустым",
                "логин не должен быть пустым"
        );
    }

    @Test
    void validateOnUpdateUser() {
        Set<ConstraintViolation<User>> constraintViolations = validator.validate(user, Marker.OnUpdate.class);
        assertThat(constraintViolations).hasSize(0);

        user.setId(0);
        constraintViolations = validator.validate(user, Marker.OnUpdate.class);
        assertThat(constraintViolations).hasSize(1);

        user.setEmail(" "); // Email не должен проверяться OnUpdate
        constraintViolations = validator.validate(user, Marker.OnUpdate.class);
        assertThat(constraintViolations).hasSize(1);

        assertThat(constraintViolations).extracting(ConstraintViolation::getMessage).containsExactlyInAnyOrder(
                "id должен быть указан и быть больше нуля"
        );
    }
}