DROP TABLE IF EXISTS users CASCADE;
CREATE TABLE IF NOT EXISTS users (
    id bigint primary key auto_increment,
    email varchar(150) NOT NULL UNIQUE,
    login varchar(150) NOT NULL UNIQUE,
    name varchar(150) NOT NULL,
    birthday date
);

DROP TABLE IF EXISTS friends CASCADE;
CREATE TABLE IF NOT EXISTS friends (
    user_id bigint REFERENCES users(id) ON DELETE CASCADE,
    friend_id bigint REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, friend_id)
);

DROP TABLE IF EXISTS genres CASCADE;
CREATE TABLE IF NOT EXISTS genres (
    id integer primary key auto_increment,
    name varchar(150) NOT NULL UNIQUE
);

DROP TABLE IF EXISTS MPA CASCADE;
CREATE TABLE IF NOT EXISTS MPA (
    id integer primary key auto_increment,
    name varchar(40) NOT NULL UNIQUE
);

DROP TABLE IF EXISTS films CASCADE;
CREATE TABLE IF NOT EXISTS films (
    id bigint primary key auto_increment,
    name varchar(150) NOT NULL,
    description varchar(150) NOT NULL,
    releaseDate date NOT NULL,
    duration integer NOT NULL CHECK (duration > 0),
    MPA_id integer REFERENCES MPA(id)
);

DROP TABLE IF EXISTS likes CASCADE;
CREATE TABLE IF NOT EXISTS likes (
    film_id bigint REFERENCES films(id) ON DELETE CASCADE,
    user_id bigint REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, user_id)
);

DROP TABLE IF EXISTS film_genre CASCADE;
CREATE TABLE IF NOT EXISTS film_genre (
    film_id bigint REFERENCES films(id) ON DELETE CASCADE,
    genre_id integer REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, genre_id)
);

DROP TABLE IF EXISTS reviews CASCADE;
CREATE TABLE IF NOT EXISTS reviews (
    review_id bigint primary key auto_increment,
    content varchar(200) NOT NULL,
    is_positive boolean NOT NULL,
    user_id bigint REFERENCES users(id) ON DELETE CASCADE,
    film_id bigint REFERENCES films(id) ON DELETE CASCADE
);

DROP TABLE IF EXISTS useful_tab CASCADE;
CREATE TABLE IF NOT EXISTS useful_tab (
    review_id bigint REFERENCES reviews(review_id) ON DELETE CASCADE,
    user_id bigint REFERENCES users(id) ON DELETE CASCADE,
    useful_flag integer NOT NULL
);

DROP TABLE IF EXISTS directors CASCADE;
CREATE TABLE IF NOT EXISTS directors (
    id bigint primary key auto_increment,
    name varchar(150) NOT NULL
);

DROP TABLE IF EXISTS film_director CASCADE;
CREATE TABLE IF NOT EXISTS film_director (
    film_id bigint REFERENCES films(id) ON DELETE CASCADE,
    director_id bigint REFERENCES directors(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, director_id)
);

DROP TABLE IF EXISTS eventType CASCADE;
CREATE TABLE IF NOT EXISTS eventType (
    id INTEGER primary key auto_increment,
    name varchar(40) NOT NULL UNIQUE
);

DROP TABLE IF EXISTS operationType CASCADE;
CREATE TABLE IF NOT EXISTS operationType (
    id INTEGER primary key auto_increment,
    name varchar(40) NOT NULL UNIQUE
);

--создание таблицы "Ленты событий" пользователя
DROP TABLE IF EXISTS userEventFeed CASCADE;
CREATE TABLE IF NOT EXISTS userEventFeed (
    event_id BIGINT PRIMARY KEY auto_increment,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    timeline BIGINT NOT NULL,
    event_type_id INTEGER NOT NULL REFERENCES eventType(id) ON DELETE CASCADE,
    operation_id INTEGER NOT NULL REFERENCES operationType(id) ON DELETE CASCADE,
    entity_id BIGINT NOT NULL
);