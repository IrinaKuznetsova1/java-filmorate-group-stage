package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotations.Marker;

@Data
@AllArgsConstructor
public class Director {
    @Positive(groups = Marker.OnUpdate.class, message = "id должен быть указан и быть больше нуля")
    private long id;
    @NotBlank(message = "имя не должно быть пустым")
    private String name;
}
