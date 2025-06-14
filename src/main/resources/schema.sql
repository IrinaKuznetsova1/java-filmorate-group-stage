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
    user_id bigint REFERENCES users(id),
    friend_id bigint REFERENCES users(id),
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
    film_id bigint REFERENCES films(id),
    user_id bigint REFERENCES users(id),
    PRIMARY KEY (film_id, user_id)
);

DROP TABLE IF EXISTS film_genre CASCADE;
CREATE TABLE IF NOT EXISTS film_genre (
    film_id bigint REFERENCES films(id),
    genre_id integer REFERENCES genres(id),
    PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS reviews (
    review_id bigint primary key auto_increment,
    content varchar(200) NOT NULL,
    is_positive boolean NOT NULL,
    user_id bigint REFERENCES users(id),
    film_id bigint REFERENCES films(id)
);


CREATE TABLE IF NOT EXISTS useful_tab (
    review_id bigint REFERENCES reviews(review_id),
    user_id bigint REFERENCES users(id),
    useful_flag integer NOT NULL
);

