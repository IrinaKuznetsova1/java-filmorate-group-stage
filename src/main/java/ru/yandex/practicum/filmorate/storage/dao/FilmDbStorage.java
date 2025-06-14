package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dao.mappers.FilmRowMapper;

import java.sql.Date;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private final FilmGenreDbStorage filmGenreDb;
    private final LikesDbStorage likesDb;
    private final MpaDbStorage mpaDb;
    private final GenreDbStorage genreDb;

    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String FIND_MOST_POPULAR_QUERY = "SELECT f.id, f.name, f.description, f.releaseDate, " +
            "f.duration, f.MPA_id FROM films AS f " +
            "LEFT JOIN (SELECT film_id, COUNT(user_id) AS like_count " +
            "FROM likes " +
            "GROUP BY film_id) AS top_films ON f.id = top_films.film_id " +
            "ORDER BY COALESCE(top_films.like_count, 0) DESC " +
            "LIMIT ?";

    private static final String INSERT_QUERY = "INSERT INTO films(name, description, releaseDate, duration, MPA_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, releaseDate = ?, " +
            "duration = ?, MPA_id = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM films WHERE id = ?";

    @Autowired
    public FilmDbStorage(
            JdbcTemplate jdbc,
            FilmRowMapper mapper,
            FilmGenreDbStorage filmGenreDb,
            LikesDbStorage likesDb,
            MpaDbStorage mpaDb,
            GenreDbStorage genreDb
    ) {
        super(jdbc, mapper);
        this.filmGenreDb = filmGenreDb;
        this.likesDb = likesDb;
        this.mpaDb = mpaDb;
        this.genreDb = genreDb;
    }

    @Override
    public Collection<Film> getAll() {
        final Collection<Film> films = findMany(FIND_ALL_QUERY);
        films.forEach(film -> {
            addLikes(film);
            addGenres(film);
            film.setMpa(mpaDb.getById(film.getMpa().getId()));
        });
        return films;
    }

    private Film addLikes(Film film) {
        likesDb.getAllByFilmId(film.getId())
                .forEach(film::saveId);
        return film;
    }

    private void addGenres(Film film) {
        filmGenreDb.getAllByFilmId(film.getId())
                .forEach(film::addGenre);
    }

    @Override
    public Film getById(long id) {
        final Optional<Film> filmOptional = findOne(FIND_BY_ID_QUERY, id);
        if (filmOptional.isEmpty())
            throw new NotFoundException("Фильм с id " + id + " не найден.");
        Film film = filmOptional.get();
        addLikes(film);
        addGenres(film);
        film.setMpa(mpaDb.getById(film.getMpa().getId()));
        return film;
    }

    @Override
    public Film save(Film film) {
        //проверка корректности mpa.id и добавление в фильм mpa.name
        film.setMpa(mpaDb.getById(film.getMpa().getId()));

        //проверка списка genres
        film.getGenres().forEach(
                genre -> genre.setName(genreDb.getNameById(genre.getId()))); //getNameById() проверит корректность genre.id и вернет genre.name
        //добавить фильм в films
        final long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(id);
        //добавить в film_genre после добавления в film, так как нужен film_id
        film.getGenres().forEach(genre -> filmGenreDb.saveId(film.getId(), genre.getId()));

        log.info("Объект сохранен в таблицу films.");
        return film;
    }

    @Override
    public Film saveUpdatedObject(Film film) {
        //проверка корректности mpa.id и добавление в фильм mpa.name
        film.setMpa(mpaDb.getById(film.getMpa().getId()));

        //проверка списка genres
        if (!film.getGenres().isEmpty()) {
            film.getGenres().forEach(genre -> genre.setName(genreDb.getNameById(genre.getId()))); //getNameById() проверит корректность genre.id и вернет genre.name);

            //если id жанров указаны верно, то в film_genre удалить старые жанры и добавить новые, добавить в film genre c корректным genre.name
            filmGenreDb.removeGenresByFilmId(film.getId());
            film.getGenres().forEach(genre -> filmGenreDb.saveId(film.getId(), genre.getId()));
        }
        //обновить фильм в films
        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        //добавить лайки и вернуть film
        return addLikes(film);
    }

    public Film saveId(long filmId, long userId) {
        likesDb.saveId(filmId, userId);
        return getById(filmId);
    }

    @Override
    public Film removeId(long filmId, long idForRm) {
        likesDb.removeId(filmId, idForRm);
        return getById(filmId);
    }

    @Override
    public Collection<Film> findTheMostPopular(long count) {
        final Collection<Film> films = findMany(FIND_MOST_POPULAR_QUERY, count);
        films.forEach(film -> {
            addLikes(film);
            addGenres(film);
            film.setMpa(mpaDb.getById(film.getMpa().getId()));
        });
        return films;
    }

    @Override
    public void delete(long userId) {
        super.delete(DELETE_QUERY, userId);
    }
}
