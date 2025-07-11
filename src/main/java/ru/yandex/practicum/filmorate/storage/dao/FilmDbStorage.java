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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private final FilmGenreDbStorage filmGenreDb;
    private final LikesDbStorage likesDb;
    private final MpaDbStorage mpaDb;
    private final GenreDbStorage genreDb;
    private final FilmDirectorDbStorage filmDirectorDb;

    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String FIND_MOST_POPULAR_QUERY = "SELECT * FROM films AS f " +
            "LEFT JOIN ( " +
            "SELECT film_id, COUNT(user_id) AS count_likes " +
            "FROM likes " +
            "GROUP BY FILM_ID ) AS mp " +
            "ON mp.film_id = f.id " +
            "WHERE (? IS NULL OR EXTRACT (YEAR from f.releaseDate) = ?) " +
            "AND (? IS NULL OR f.id IN ( " +
            "SELECT film_id " +
            "FROM film_genre " +
            "WHERE genre_id = ?)) " +
            "ORDER BY mp.count_likes DESC " +
            "LIMIT ?";
    private static final String FIND_BY_DIRECTOR_SORTED_BY_LIKES = "SELECT f.* FROM films f " +
            "JOIN film_director fd ON f.id = fd.film_id " +
            "LEFT JOIN likes l ON f.id = l.film_id " +
            "WHERE fd.director_id = ? " +
            "GROUP BY f.id " +
            "ORDER BY COUNT(l.user_id) DESC";
    private static final String FIND_BY_DIRECTOR_SORTED_BY_YEAR = "SELECT f.* FROM films f " +
            "JOIN film_director fd ON f.id = fd.film_id " +
            "WHERE fd.director_id = ? " +
            "ORDER BY f.releaseDate";
    private static final String FIND_COMMON_FILMS_QUERY =
            "SELECT f.id, f.name, f.description, f.releaseDate, f.duration, f.MPA_id " +
                    "FROM films f " +
                    "JOIN likes l1 ON f.id = l1.film_id AND l1.user_id = ? " +
                    "JOIN likes l2 ON f.id = l2.film_id AND l2.user_id = ? " +
                    "LEFT JOIN (SELECT film_id, COUNT(user_id) AS like_count FROM likes GROUP BY film_id) lc ON f.id = lc.film_id " +
                    "ORDER BY COALESCE(lc.like_count, 0) DESC";

    private static final String FIND_RECOMMENDATIONS_QUERY = """
            SELECT * FROM films f
            RIGHT JOIN (
            	SELECT DISTINCT l.film_id AS films FROM likes AS l
            	RIGHT JOIN (
            		SELECT user_id FROM likes
            		WHERE film_id IN (SELECT film_id FROM likes WHERE user_id = ?)	AND user_id <> ?
            		GROUP BY user_id
            		ORDER BY count(likes.film_id) DESC) AS users
            	ON users.user_id = l.user_id
            	WHERE l.film_id NOT IN (
            	SELECT film_id FROM likes WHERE user_id = ?)) AS f1
            ON f1.films = f.id;""";
    private static final String INSERT_QUERY = "INSERT INTO films(name, description, releaseDate, duration, MPA_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, releaseDate = ?, " +
            "duration = ?, MPA_id = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM films WHERE id = ?";

    private static final String FIND_MOST_POPULAR_BY_NAME = "SELECT f.*, COUNT(ls.user_id) AS likes_count FROM films AS f " +
            "LEFT JOIN likes AS ls ON f.id = ls.film_id WHERE LOWER(f.name) LIKE LOWER(?) GROUP BY f.id ORDER BY likes_count DESC";
    private static final String FIND_MOST_POPULAR_BY_DIRECTOR = "SELECT f.*, COUNT(ls.user_id) AS likes_count " +
            "FROM films AS f LEFT JOIN likes AS ls ON f.id = ls.film_id LEFT JOIN film_director AS fd ON f.id = fd.film_id " +
            "LEFT JOIN directors AS d ON fd.director_id = d.id WHERE LOWER(d.name) LIKE LOWER(?) GROUP BY f.id " +
            "ORDER BY likes_count DESC";
    private static final String FIND_MOST_POPULAR_BY_DIRECTOR_AND_TITLE = "SELECT f.id, f.name, f.description, f.releaseDate, f.duration, f.MPA_id " +
            "FROM films f " +
            "LEFT JOIN film_director fd ON f.id = fd.film_id " +
            "LEFT JOIN directors d ON fd.director_id = d.id " +
            "WHERE (LOWER(f.name) LIKE LOWER(?) OR LOWER(d.name) LIKE LOWER(?)) " +
            "GROUP BY f.id, f.name, f.description, f.releaseDate, f.duration, f.MPA_id " +
            "ORDER BY (SELECT COUNT(*) FROM likes WHERE film_id = f.id) DESC";

    @Autowired
    public FilmDbStorage(
            JdbcTemplate jdbc,
            FilmRowMapper mapper,
            FilmGenreDbStorage filmGenreDb,
            LikesDbStorage likesDb,
            MpaDbStorage mpaDb,
            GenreDbStorage genreDb,
            FilmDirectorDbStorage filmDirectorDb
    ) {
        super(jdbc, mapper);
        this.filmGenreDb = filmGenreDb;
        this.likesDb = likesDb;
        this.mpaDb = mpaDb;
        this.genreDb = genreDb;
        this.filmDirectorDb = filmDirectorDb;
    }

    @Override
    public Collection<Film> getAll() {
        final Collection<Film> films = findMany(FIND_ALL_QUERY);
        films.forEach(this::loadAdditionalData);
        return films;
    }

    @Override
    public Film getById(long id) {
        final Optional<Film> filmOptional = findOne(FIND_BY_ID_QUERY, id);
        if (filmOptional.isEmpty()) {
            throw new NotFoundException("Фильм с id " + id + " не найден.");
        }
        Film film = filmOptional.get();
        loadAdditionalData(film);
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

        // добавить режиссеров
        film.getDirectors().forEach(director -> filmDirectorDb.addDirectorToFilm(film.getId(), director.getId()));

        log.info("Фильм сохранен в базу данных с id: {}", id);
        return film;
    }

    @Override
    public Film saveUpdatedObject(Film film) {
        //проверка корректности mpa.id и добавление в фильм mpa.name
        film.setMpa(mpaDb.getById(film.getMpa().getId()));

        //проверка списка genres
        film.getGenres().forEach(genre -> genre.setName(genreDb.getNameById(genre.getId()))); //getNameById() проверит корректность genre.id и вернет genre.name);

        //если id жанров указаны верно, то в film_genre удалить старые жанры и добавить новые, добавить в film genre c корректным genre.name
        filmGenreDb.removeGenresByFilmId(film.getId());
        film.getGenres().forEach(genre -> filmGenreDb.saveId(film.getId(), genre.getId()));

        // обновить режиссеров
        filmDirectorDb.removeDirectorsFromFilm(film.getId());
        film.getDirectors().forEach(director -> filmDirectorDb.addDirectorToFilm(film.getId(), director.getId()));


        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        return film;
    }

    @Override
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
    public Collection<Film> findTheMostPopular(long count, Integer genreId, Integer year) {
        final Collection<Film> films = findMany(FIND_MOST_POPULAR_QUERY, year, year, genreId, genreId, count);
        films.forEach(this::loadAdditionalData);
        return films;
    }

    @Override
    public Collection<Film> getFilmsByDirectorSortedByLikes(long directorId) {
        final Collection<Film> films = findMany(FIND_BY_DIRECTOR_SORTED_BY_LIKES, directorId);
        films.forEach(this::loadAdditionalData);
        return films;
    }

    @Override
    public Collection<Film> getFilmsByDirectorSortedByYear(long directorId) {
        final Collection<Film> films = findMany(FIND_BY_DIRECTOR_SORTED_BY_YEAR, directorId);
        films.forEach(this::loadAdditionalData);
        return films;
    }

    @Override
    public Collection<Film> findCommonFilms(long userId, long friendId) {
        final Collection<Film> films = findMany(FIND_COMMON_FILMS_QUERY, userId, friendId);
        films.forEach(this::loadAdditionalData);
        return films;
    }

    @Override
    public void delete(long filmId) {
        super.delete(DELETE_QUERY, filmId);
    }

    public Collection<Film> findByFilmNameAndOrDirectorAndBackPopularFilms(String query, String by) {
        Set<String> components = Arrays.stream(by.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        Set<String> allowedValues = Set.of("director", "title");
        if (!allowedValues.containsAll(components)) {
            throw new IllegalArgumentException("Неверные параметры ввода, ожидаются: 'director' и/или 'title'");
        }
        Collection<Film> films = null;

        try {
            if (components.contains("director") && components.contains("title")) {
                log.info("Поиск одновременно по director и title");
                films = findMany(FIND_MOST_POPULAR_BY_DIRECTOR_AND_TITLE, "%" + query.toLowerCase() + "%", "%" + query.toLowerCase() + "%");
            } else if (components.contains("director")) {
                log.info("Поиск только по полю director");
                films = findMany(FIND_MOST_POPULAR_BY_DIRECTOR, "%" + query.toLowerCase() + "%");
            } else if (components.contains("title")) {
                log.info("Поиск только по полю title");
                films = findMany(FIND_MOST_POPULAR_BY_NAME, "%" + query.toLowerCase() + "%");
            }
        } catch (Exception e) {
            log.info("Ничего не найдено по указанному поиску");
            return Collections.emptyList();
        }
        films.forEach(this::loadAdditionalData);
        return films;
    }

    private Film loadAdditionalData(Film film) {
        likesDb.getAllByFilmId(film.getId()).forEach(film::saveId);
        filmGenreDb.getAllByFilmId(film.getId()).forEach(film::addGenre);
        film.setDirectors(filmDirectorDb.getDirectorsByFilmId(film.getId()));
        film.setMpa(mpaDb.getById(film.getMpa().getId()));
        return film;
    }

    @Override
    public Collection<Film> findRecommendations(long userId) {
        final Collection<Film> films = findMany(FIND_RECOMMENDATIONS_QUERY, userId, userId, userId);
        films.forEach(this::loadAdditionalData);
        return films;
    }
}