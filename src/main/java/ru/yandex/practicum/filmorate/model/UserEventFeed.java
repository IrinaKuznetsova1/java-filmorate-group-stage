package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEventFeed {

    @NotNull
    @Positive
    private Long eventId;

    @NotNull
    @Positive
    private Long userId;

    @NotNull
    @Positive
    private Long timestamp;

    @Pattern(regexp = "^(LIKE|REVIEW|FRIEND)$", message = "eventType может быть только LIKE, REVIEW или FRIEND")
    private String eventType;

    @Pattern(regexp = "^(REMOVE|ADD|UPDATE)$", message = "operation может быть только REMOVE, ADD или UPDATE")
    private String operation;

    //Идентификатор сущности, с которой произошло событие -> friend_id, film_id, review_id
    @NotNull
    @Positive
    private Long entityId;
}
