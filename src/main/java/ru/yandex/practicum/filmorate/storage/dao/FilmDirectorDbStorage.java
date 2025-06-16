package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class FilmDirectorDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final DirectorDbStorage directorDbStorage;

    public void addDirectorToFilm(long filmId, long directorId) {
        String sql = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, directorId);
    }

    public Set<Director> getDirectorsByFilmId(long filmId) {
        String sql = "SELECT director_id FROM film_director WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.query(
                sql,
                (rs, rowNum) -> directorDbStorage.getById(rs.getLong("director_id")).orElseThrow(),
                filmId
        ));
    }

    public void removeDirectorsFromFilm(long filmId) {
        String sql = "DELETE FROM film_director WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }
}