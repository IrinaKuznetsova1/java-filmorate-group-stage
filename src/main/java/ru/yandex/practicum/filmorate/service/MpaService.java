package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.storage.dao.MpaDbStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaDbStorage mpaDbStorage;

    public Collection<MpaDto> findAll() {
        return mpaDbStorage.getAll()
                .stream()
                .map(MpaMapper::mapToMpaDto)
                .toList();
    }

    public MpaDto findById(long id) {
        return MpaMapper.mapToMpaDto(mpaDbStorage.getById(id));
    }
}
