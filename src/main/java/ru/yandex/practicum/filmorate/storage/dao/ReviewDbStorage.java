package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.dao.mappers.ReviewRowMapper;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
@Qualifier("reviewDbStorage")
public class ReviewDbStorage extends BaseDbStorage<Review> implements ReviewStorage {

    private static final String INSERT_QUERY = "INSERT INTO reviews(content, is_positive, user_id, film_id) " +
            "VALUES (?, ?, ?, ?)";
    private static final String FIND_BY_ID_QUERY = "SELECT r.review_id" +
            ", r.content" +
            ", r.is_positive" +
            ", r.user_id" +
            ", r.film_id" +
            ", COALESCE(SUM(u.useful_flag), 0) as useful " +
            "FROM reviews as r " +
            "LEFT JOIN useful_tab as u ON r.review_id = u.review_id " +
            "WHERE r.review_id = ? " +
            "GROUP BY r.review_id, r.content, r.is_positive, r.user_id, r.film_id " +
            "ORDER BY useful DESC";
    private static final String FIND_ALL_QUERY = "SELECT r.review_id" +
            ", r.content" +
            ", r.is_positive" +
            ", r.user_id" +
            ", r.film_id" +
            ", COALESCE(SUM(u.useful_flag), 0) as useful " +
            "FROM reviews as r " +
            "LEFT JOIN useful_tab as u ON r.review_id = u.review_id " +
            "GROUP BY r.review_id, r.content, r.is_positive, r.user_id, r.film_id " +
            "ORDER BY useful DESC " +
            "LIMIT ?";
    private static final String FIND_BY_ID_LIMIT_QUERY = "SELECT r.review_id" +
            ", r.content" +
            ", r.is_positive" +
            ", r.user_id" +
            ", r.film_id" +
            ", COALESCE(SUM(u.useful_flag), 0) as useful " +
            "FROM reviews as r " +
            "LEFT JOIN useful_tab as u ON r.review_id = u.review_id " +
            "WHERE r.film_id = ? " +
            "GROUP BY r.review_id, r.content, r.is_positive, r.user_id, r.film_id " +
            "ORDER BY useful DESC " +
            "LIMIT ?";
    private static final String UPDATE_QUERY = "UPDATE reviews SET content = ?" +
            ", is_positive = ?" +
            "WHERE review_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM reviews WHERE review_id = ?";
    private static final String INSERT_LIKE_REVIEW_QUERY = "INSERT INTO useful_tab(review_id, user_id, useful_flag) " +
            "VALUES (?, ?, ?)";

    private static final String DELETE_USER_LIKE_QUERY = "DELETE FROM useful_tab WHERE review_id = ? and user_id = ?";

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbc, ReviewRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Collection<Review> findReviews(Long filmId, int count) {
        Collection<Review> reviews;
        if (filmId == null) {
            reviews = findMany(FIND_ALL_QUERY, count);
        } else {
            reviews = findMany(FIND_BY_ID_LIMIT_QUERY, filmId, count);
        }
        return reviews;
    }

    @Override
    public Review save(Review review) {
        final long id = insert(
                INSERT_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId()
        );

        review.setReviewId(id);

        log.info("Объект сохранен в таблицу reviews.");
        return review;
    }

    @Override
    public Review getById(long id) {
        final Optional<Review> reviewOptional = findOne(FIND_BY_ID_QUERY, id);

        if (reviewOptional.isEmpty()) {
            throw new NotFoundException("Отзыв с id " + id + " не найден.");
        }

        log.info("Получен объект из reviews с id: {}.", id);
        return reviewOptional.get();
    }

    @Override
    public Review saveUpdatedObject(Review review) {
        update(
                UPDATE_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );

        log.info("Обновлен объект из reviews с id: {}.", review.getReviewId());
        return getById(review.getReviewId());
    }

    public void remove(long reviewId) {
        log.info("Удаление отзыва с id {}.", reviewId);
        delete(DELETE_QUERY, reviewId);
    }

    @Override
    public void saveLike(long reviewId, long userId) {
        log.info("Добавление лайка пользователя {} на отзыв с id {}.", userId, reviewId);
        delete(DELETE_USER_LIKE_QUERY, reviewId, userId);
        update(INSERT_LIKE_REVIEW_QUERY,
                reviewId,
                userId,
                1);
    }

    @Override
    public void saveDislike(long reviewId, long userId) {
        log.info("Добавление дизлайка пользователя {} на отзыв с id {}.", userId, reviewId);
        delete(DELETE_USER_LIKE_QUERY, reviewId, userId);
        update(INSERT_LIKE_REVIEW_QUERY,
                reviewId,
                userId,
                -1);
    }

    public void removeUserLike(long reviewId, long userId) {
        log.info("Удаление пользователем {} лайка с id {}.", userId, reviewId);
        delete(DELETE_USER_LIKE_QUERY, reviewId, userId);
    }
}
