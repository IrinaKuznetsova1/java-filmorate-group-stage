package ru.yandex.practicum.filmorate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import ch.qos.logback.classic.Level;
import org.slf4j.LoggerFactory;

@Slf4j
@SpringBootApplication
public class FilmorateApplication {

    public static void main(String[] args) {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.DEBUG);
        SpringApplication.run(FilmorateApplication.class, args);
        log.info("Запущено приложение 'FilmorateApplication'.");
    }
}
