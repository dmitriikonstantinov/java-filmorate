DELETE FROM friendship;
DELETE FROM film_likes;
DELETE FROM film_genre;
DELETE FROM films;
DELETE FROM users;
DELETE FROM genres;
DELETE FROM mpa_ratings;

MERGE INTO mpa_ratings (id, name) VALUES (1, 'G');
MERGE INTO mpa_ratings (id, name) VALUES (2, 'PG');
MERGE INTO mpa_ratings (id, name) VALUES (3, 'PG-13');
MERGE INTO mpa_ratings (id, name) VALUES (4, 'R');
MERGE INTO mpa_ratings (id, name) VALUES (5, 'NC-17');

MERGE INTO genres (id, name) VALUES (1, 'Комедия');
MERGE INTO genres (id, name) VALUES (2, 'Драма');
MERGE INTO genres (id, name) VALUES (3, 'Мультфильм');
MERGE INTO genres (id, name) VALUES (4, 'Триллер');
MERGE INTO genres (id, name) VALUES (5, 'Документальный');
MERGE INTO genres (id, name) VALUES (6, 'Боевик');