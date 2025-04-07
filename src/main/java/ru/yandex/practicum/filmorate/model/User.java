package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotations.Marker;
import ru.yandex.practicum.filmorate.annotations.NoSpaces;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class User {
    @Positive(groups = Marker.OnUpdate.class, message = "id должен быть указан и быть больше нуля")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private long id;

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
}
