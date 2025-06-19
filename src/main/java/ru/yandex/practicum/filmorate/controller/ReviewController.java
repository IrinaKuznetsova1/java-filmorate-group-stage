package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.annotations.Marker;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.*;

@Validated
@RestController
@RequestMapping("/reviews")
@Slf4j
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/{id}")
    public Review findReviewById(@PathVariable @Min(1) long id) {
        log.info("Получен запрос GET /films/{}.", id);
        return reviewService.findById(id);
    }

    @GetMapping
    public Collection<Review> findReviews(
            @RequestParam(required = false) Long filmId,
            @Positive @RequestParam(required = false, defaultValue = "10") int count
    ) {
        log.info("Получен запрос GET /reviews?filmId={}&count={}.", filmId, count);
        return reviewService.findReviews(filmId, count);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Validated({Marker.OnCreate.class})
    public Review create(@Valid @RequestBody Review review) {
        log.info("Получен запрос POST /reviews.");
        return reviewService.create(review);
    }

    @PutMapping
    @Validated({Marker.OnUpdate.class})
    public Review update(@Valid @RequestBody Review newReview) {
        log.info("Получен запрос PUT /reviews.");
        return reviewService.update(newReview);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable @Min(1) long id) {
        log.info("Получен запрос DELETE /films/{}.", id);
        reviewService.deleteReview(id);
    }


    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable @Min(1) long id, @PathVariable @Min(1) long userId) {
        log.info("Получен запрос PUT /reviews/{}/like/{}.", id, userId);
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable @Min(1) long id, @PathVariable @Min(1) long userId) {
        log.info("Получен запрос PUT /reviews/{}/dislike/{}.", id, userId);
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable @Min(1) long id, @PathVariable @Min(1) long userId) {
        log.info("Получен запрос DELETE /reviews/{}/like/{}.", id, userId);
        reviewService.removeUserLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable @Min(1) long id, @PathVariable @Min(1) long userId) {
        log.info("Получен запрос DELETE /reviews/{}/dislike/{}.", id, userId);
        reviewService.removeUserDislike(id, userId);
    }
}
