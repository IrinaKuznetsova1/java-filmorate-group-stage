package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.annotations.Marker;


@Data
@Slf4j
@AllArgsConstructor
public class Review {

    private Long reviewId;
    private String content;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
    private long useful = 0;

}
