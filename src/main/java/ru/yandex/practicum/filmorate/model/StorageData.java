package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.annotations.Marker;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Getter
@Setter
public class StorageData {
    @Positive(groups = Marker.OnUpdate.class, message = "id должен быть указан и быть больше нуля")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    protected long id;

    protected final Set<Long> ids = new HashSet<>();

    public void saveId(long id) {
        ids.add(id);
    }

    public void deleteId(long id) {
        ids.remove(id);
    }

    public long getNumberOfIds() {
        return ids.size();
    }

}
