-- TRUNCATE TABLE leagues;
-- TRUNCATE TABLE teams;
-- TRUNCATE TABLE games;
-- TRUNCATE TABLE games_teams;

INSERT INTO leagues (name) VALUES ('Лига 1');
INSERT INTO leagues (name) VALUES ('Лига 2');
INSERT INTO leagues (name) VALUES ('Лига 3');

INSERT INTO teams (name) VALUES ('Команда 1');
INSERT INTO teams (name) VALUES ('Команда 2');
INSERT INTO teams (name) VALUES ('Команда 3');
INSERT INTO teams (name) VALUES ('Команда 4');
INSERT INTO teams (name) VALUES ('Команда 5');

INSERT INTO games (league_id, date, parsed_at) VALUES (2, '2023-10-23 22:00:00.000000', '2023-10-23 14:13:55.000000');
INSERT INTO games (league_id, date, parsed_at) VALUES (2, '2023-10-23 22:00:00.000000', '2023-10-23 14:13:55.000000');
INSERT INTO games (league_id, date, parsed_at) VALUES (1, '2023-10-23 09:30:00.000000', '2023-10-23 14:13:55.000000');
INSERT INTO games (league_id, date, parsed_at) VALUES (1, '2023-10-23 12:30:00.000000', '2023-10-23 14:13:55.000000');
INSERT INTO games (league_id, date, parsed_at) VALUES (3, '2023-10-24 17:00:00.000000', '2023-10-24 09:41:41.000000');

INSERT INTO games_teams (game_id, team_id) VALUES (1, 4);
INSERT INTO games_teams (game_id, team_id) VALUES (1, 2);
INSERT INTO games_teams (game_id, team_id) VALUES (2, 1);
INSERT INTO games_teams (game_id, team_id) VALUES (2, 3);
INSERT INTO games_teams (game_id, team_id) VALUES (3, 5);
INSERT INTO games_teams (game_id, team_id) VALUES (3, 2);
INSERT INTO games_teams (game_id, team_id) VALUES (4, 1);
INSERT INTO games_teams (game_id, team_id) VALUES (4, 5);
INSERT INTO games_teams (game_id, team_id) VALUES (5, 1);
INSERT INTO games_teams (game_id, team_id) VALUES (5, 4);
