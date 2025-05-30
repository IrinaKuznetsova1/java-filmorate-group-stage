package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.annotations.Marker;
import ru.yandex.practicum.filmorate.annotations.NoSpaces;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class User extends StorageData {

    @NotBlank(groups = Marker.OnCreate.class, message = "e-mail не должен быть null или быть пустым")
    @Email(message = "строка должна соответствовать формату адреса электронной почты")
    private String email;

    @NotBlank(groups = Marker.OnCreate.class, message = "логин не должен быть пустым")
    @NoSpaces(message = "логин не должен содержать пробелы, переносы строки или табуляцию")
    private String login;

    private String name;

    @Past(message = "дата должна быть прошедшей")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    public User(long id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }

}
