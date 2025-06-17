package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.UserEventFeedDto;
import ru.yandex.practicum.filmorate.model.UserEventFeed;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserEventFeedDtoMapper {

    public static UserEventFeedDto mapToUserEventFeedDto(UserEventFeed userEventFeed) {
        UserEventFeedDto userEventFeedDto = new UserEventFeedDto();
        userEventFeedDto.setTimestamp(userEventFeed.getTimestamp());
        userEventFeedDto.setUserId(userEventFeed.getUserId());
        userEventFeedDto.setEventType(userEventFeed.getEventType());
        userEventFeedDto.setOperation(userEventFeed.getOperation());
        userEventFeedDto.setEventId(userEventFeed.getEventId());
        userEventFeedDto.setEntityId(userEventFeed.getEntityId());

        return userEventFeedDto;
    }
}
