package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEventFeed {

    private Long eventId;
    private Long userId;
    private Long timestamp;
    private EventType eventType;
    private Operation operation;
    private Long entityId;  //Идентификатор сущности, с которой произошло событие -> friend_id, film_id, review_id

    public enum EventType {
        LIKE,
        REVIEW,
        FRIEND
    }

    public enum Operation {
        REMOVE,
        ADD,
        UPDATE
    }
}
