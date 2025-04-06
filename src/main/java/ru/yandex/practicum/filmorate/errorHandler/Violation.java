package ru.yandex.practicum.filmorate.errorHandler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Violation {
    private final String fieldNameWithError;
    private final String message;
}
