package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.annotations.Marker;
import ru.yandex.practicum.filmorate.annotations.MinReleaseDate;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class Film extends StorageData {
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

    @NotNull(groups = Marker.OnCreate.class, message = "возрастной рейтинг должен быть указан")
    private Mpa mpa;

    private Set<Director> directors = new LinkedHashSet<>();
    private Set<Genre> genres = new LinkedHashSet<>();

    public Film(long id, String name, String description, LocalDate releaseDate, int duration, Mpa mpa) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
    }

    public void addGenre(Genre genre) {
        genres.add(genre);
    }

    public void addDirector(Director director) {
        directors.add(director);
    }
}
