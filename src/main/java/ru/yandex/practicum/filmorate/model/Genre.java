package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Genre {
    @Positive(message = "id должен быть указан и быть больше нуля")
    private long id;
    private String name;
}
