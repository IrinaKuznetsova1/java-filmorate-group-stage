package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.dao.mappers.MpaRowMapper;

import java.util.Collection;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaDbStorage.class, MpaRowMapper.class})
class MpaDbStorageTest {
    private final MpaDbStorage mpaDbStorage;


    @Test
    void getAll() {
        final Collection<Mpa> mpas = mpaDbStorage.getAll();
        assertThat(mpas.size()).isEqualTo(5);
    }

    @Test
    void getById() {
        final Mpa mpa = mpaDbStorage.getById(1);
        assertThat(mpa).hasFieldOrPropertyWithValue("id", 1);
    }
}