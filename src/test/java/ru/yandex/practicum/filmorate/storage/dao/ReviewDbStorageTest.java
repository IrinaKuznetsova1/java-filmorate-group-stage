package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.dao.mappers.*;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({ReviewDbStorage.class, ReviewRowMapper.class,
        FilmDbStorage.class, FilmRowMapper.class,
        GenreDbStorage.class, GenreRowMapper.class,
        MpaDbStorage.class, MpaRowMapper.class,
        LikesDbStorage.class, LikeRowMapper.class,
        FilmGenreDbStorage.class, FilmGenreRowMapper.class,
        UserDbStorage.class, UserRowMapper.class,
        FriendsDbStorage.class, FriendsRowMapper.class,
        FilmDirectorDbStorage.class, FilmDirectorRowMapper.class,
        DirectorDbStorage.class, DirectorRowMapper.class})
public class ReviewDbStorageTest {
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final ReviewDbStorage reviewDbStorage;
    private final JdbcTemplate jdbc;

    private final Film film1 = new Film(1, "name1", "description1", LocalDate.now(), 60, new Mpa(1, "G"));
    private final Film film2 = new Film(2, "name2", "description2", LocalDate.now(), 60, new Mpa(2, "PG"));
    private final Film film3 = new Film(3, "name3", "description3", LocalDate.now(), 60, new Mpa(3, "PG-13"));

    private final User user1 = new User(1, "test1@mail.ru", "Login1", "Name1", LocalDate.now());
    private final User user2 = new User(2, "test2@mail.ru", "Login2", "Name2", LocalDate.now());
    private final User user3 = new User(3, "test3@mail.ru", "Login3", "Name3", LocalDate.now());

    @BeforeEach
    void setup() {
        film1.addGenre(new Genre(1, "Комедия"));
        film1.addGenre(new Genre(3, "Мультфильм"));
        film1.addGenre(new Genre(5, "Документальный"));

        film2.addGenre(new Genre(3, "Мультфильм"));

        film3.addGenre(new Genre(5, "Документальный"));

        filmDbStorage.save(film1);
        filmDbStorage.save(film2);
        filmDbStorage.save(film3);
    }

    @AfterEach
    void clear() {
        jdbc.update("DELETE FROM film_genre");
        jdbc.update("DELETE FROM likes");
        jdbc.update("DELETE FROM film_director");
        jdbc.update("DELETE FROM films");
        jdbc.update("DELETE FROM users");
    }

    @Test
    void findReviews() {
        userDbStorage.save(user1);
        userDbStorage.save(user2);
        userDbStorage.save(user3);

        filmDbStorage.saveId(film1.getId(), user1.getId());
        filmDbStorage.saveId(film2.getId(), user1.getId());

        Review review1 = new Review(1L
                , "Test Review 1",
                true,
                user1.getId(),
                film1.getId(),
                0);
        Review review2 = new Review(2L
                , "Test Review 2",
                false,
                user2.getId(),
                film1.getId(),
                0);

        reviewDbStorage.save(review1);
        reviewDbStorage.save(review2);

        Collection<Review> reviews = reviewDbStorage.findReviews(review1.getFilmId(), 10);
        assertThat(reviews.size()).isEqualTo(2);

        reviews.forEach(
                review -> {
                    assertEquals(0, review.getUseful());
                });
    }

    @Test
    void getById() {
        userDbStorage.save(user1);
        userDbStorage.save(user2);
        userDbStorage.save(user3);

        filmDbStorage.saveId(film1.getId(), user1.getId());
        filmDbStorage.saveId(film2.getId(), user1.getId());

        Review review1 = new Review(1L
                , "Test Review 1",
                true,
                user1.getId(),
                film1.getId(),
                0);
        Review review2 = new Review(1L
                , "Test Review 1",
                true,
                user1.getId(),
                film1.getId(),
                0);

        Review reviewSave = reviewDbStorage.save(review1);
        review2.setReviewId(reviewSave.getReviewId());

        Review review3 = reviewDbStorage.getById(review1.getReviewId());
        assertThat(review3).isEqualTo(review2);
    }

    @Test
    void saveUpdatedObject() {
        userDbStorage.save(user1);
        userDbStorage.save(user2);
        userDbStorage.save(user3);

        filmDbStorage.saveId(film1.getId(), user1.getId());
        filmDbStorage.saveId(film2.getId(), user1.getId());

        Review review1 = new Review(1L
                , "Test Review 1",
                true,
                user1.getId(),
                film1.getId(),
                0);
        Review review2 = new Review(1L
                , "Test Review 2",
                false,
                user1.getId(),
                film1.getId(),
                0);

        Review review3 = new Review(1L
                , "Test Review 2",
                false,
                user1.getId(),
                film1.getId(),
                0);

        Review reviewSave = reviewDbStorage.save(review1);
        review2.setReviewId(reviewSave.getReviewId());
        review3.setReviewId(reviewSave.getReviewId());

        reviewDbStorage.saveUpdatedObject(review2);
        assertThat(review2).isEqualTo(review3);
    }

    @Test
    void remove() {
        userDbStorage.save(user1);
        userDbStorage.save(user2);
        userDbStorage.save(user3);

        filmDbStorage.saveId(film1.getId(), user1.getId());
        filmDbStorage.saveId(film2.getId(), user1.getId());

        Review review1 = new Review(1L
                , "Test Review 1",
                true,
                user1.getId(),
                film1.getId(),
                0);
        Review review2 = new Review(2L
                , "Test Review 2",
                false,
                user1.getId(),
                film1.getId(),
                0);

        Review review1Save = reviewDbStorage.save(review1);
        Review review2Save = reviewDbStorage.save(review2);

        Collection<Review> reviews = reviewDbStorage.findReviews(review1.getFilmId(), 10);

        assertThat(reviews.size()).isEqualTo(2);
        reviewDbStorage.remove(review1Save.getReviewId());

        Collection<Review> reviewsUpd = reviewDbStorage.findReviews(review1.getFilmId(), 10);
        assertThat(reviewsUpd.size()).isEqualTo(1);
    }

    @Test
    void saveLike() {
        userDbStorage.save(user1);
        userDbStorage.save(user2);

        filmDbStorage.saveId(film1.getId(), user1.getId());

        Review review1 = new Review(1L
                , "Test Review 1",
                true,
                user1.getId(),
                film1.getId(),
                0);

        reviewDbStorage.save(review1);

        reviewDbStorage.saveLike(review1.getReviewId(), user1.getId());
        reviewDbStorage.saveLike(review1.getReviewId(), user2.getId());

        Review review = reviewDbStorage.getById(review1.getReviewId());
        assertThat(review.getUseful()).isEqualTo(2);
    }

    @Test
    void saveDislike() {
        userDbStorage.save(user1);
        userDbStorage.save(user2);

        filmDbStorage.saveId(film1.getId(), user1.getId());

        Review review1 = new Review(1L
                , "Test Review 1",
                true,
                user1.getId(),
                film1.getId(),
                0);

        reviewDbStorage.save(review1);

        reviewDbStorage.saveDislike(review1.getReviewId(), user1.getId());
        reviewDbStorage.saveDislike(review1.getReviewId(), user2.getId());

        Review review = reviewDbStorage.getById(review1.getReviewId());
        assertThat(review.getUseful()).isEqualTo(-2);
    }

    @Test
    void removeUserLike() {
        userDbStorage.save(user1);
        userDbStorage.save(user2);

        filmDbStorage.saveId(film1.getId(), user1.getId());

        Review review1 = new Review(1L
                , "Test Review 1",
                true,
                user1.getId(),
                film1.getId(),
                0);

        reviewDbStorage.save(review1);

        reviewDbStorage.saveLike(review1.getReviewId(), user1.getId());
        reviewDbStorage.saveLike(review1.getReviewId(), user2.getId());


        Review review = reviewDbStorage.getById(review1.getReviewId());
        assertThat(review.getUseful()).isEqualTo(2);

        reviewDbStorage.removeUserLike(review1.getReviewId(), user1.getId());

        Review reviewUpd = reviewDbStorage.getById(review1.getReviewId());
        assertThat(reviewUpd.getUseful()).isEqualTo(1);
    }
}
