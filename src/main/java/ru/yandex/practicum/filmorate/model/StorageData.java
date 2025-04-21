package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.annotations.Marker;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;

import java.util.HashSet;
import java.util.Set;

@Data
@Slf4j
public class StorageData {

    @Positive(groups = Marker.OnUpdate.class, message = "id должен быть указан и быть больше нуля")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    protected long id;

    protected final Set<Long> savedIds = new HashSet<>();

    public void saveId(long id) {
        if (savedIds.contains(id)) {
            log.warn("Выброшено исключение DuplicatedDataException, объект с id:{} уже сохранен.", id);
            throw new DuplicatedDataException("id", "Объект с id " + id + " уже сохранен.");
        }
        savedIds.add(id);
    }

    public void deleteId(long id) {
        savedIds.remove(id);
    }

    public long getNumberOfIds() {
        return savedIds.size();
    }

}
