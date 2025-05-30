package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Mpa {
    @Positive(message = "id должен быть указан и быть больше нуля")
    private int id;
    private String name;

    public Mpa(int id) {
        this.id = id;
    }
}

