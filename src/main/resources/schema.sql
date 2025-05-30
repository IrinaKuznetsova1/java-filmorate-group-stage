CREATE TABLE IF NOT EXISTS users (
    id bigint primary key auto_increment,
    email varchar(150) NOT NULL UNIQUE,
    login varchar(150) NOT NULL UNIQUE,
    name varchar(150) NOT NULL,
    birthday date
);

CREATE TABLE IF NOT EXISTS friends (
    id bigint primary key auto_increment,
    user_id bigint REFERENCES users(id),
    friend_id bigint REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS genres (
    id bigint primary key auto_increment,
    name varchar(150) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS MPA (
    id integer primary key auto_increment,
    name varchar(40) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS films (
    id bigint primary key auto_increment,
    name varchar(150) NOT NULL,
    description varchar(150) NOT NULL,
    releaseDate date NOT NULL,
    duration integer NOT NULL CHECK (duration > 0),
    MPA_id integer REFERENCES MPA(id)
);

CREATE TABLE IF NOT EXISTS likes (
    id bigint primary key auto_increment,
    film_id bigint REFERENCES films(id),
    user_id bigint REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS film_genre (
    id bigint primary key auto_increment,
    film_id bigint REFERENCES films(id),
    genre_id bigint REFERENCES genres(id)
);

