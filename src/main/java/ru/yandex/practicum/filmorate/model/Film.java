package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotations.Marker;
import ru.yandex.practicum.filmorate.annotations.MinReleaseDate;

import java.time.LocalDate;

/**
 * Film.
 */

@Data
@AllArgsConstructor
public class Film {
    @Positive(groups = Marker.OnUpdate.class, message = "id должен быть указан и быть больше нуля")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private long id;

    @NotBlank(groups = Marker.OnCreate.class, message = "имя не должно быть пустым")
    private String name;

    @NotBlank(groups = Marker.OnCreate.class, message = "описание не должно быть пустым")
    @Size(max = 200, message = "максимальная длина описания - 200 символов")
    private String description;

    @NotNull(groups = Marker.OnCreate.class, message = "дата должна быть указана")
    @MinReleaseDate(groups = {Marker.OnCreate.class, Marker.OnUpdate.class}, message = "дата должна быть не ранее 28.12.1985")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @Positive(groups = Marker.OnCreate.class, message = "продолжительность должна быть больше 0")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private int duration;
}
