package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserEventFeedDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long timestamp;

    private Long userId;

    private String eventType;

    private String operation;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long eventId;

    //Идентификатор сущности, с которой произошло событие -> friend_id, film_id, review_id
    private Long entityId;
}
