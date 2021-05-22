INSERT INTO player
VALUES (1, 'Gil', 'Pratte', '5121231235', 'gilpratte@texastoc.com',
        '$2a$10$RRxUYMgQJu99pCMsny6UP.b8I7pheP5Keq4D1JGlY9tken4LLXKi2'),
       (2, 'Guest', 'User', '5121231235', 'guest@texastoc.com',
        '$2a$10$RRxUYMgQJu99pCMsny6UP.b8I7pheP5Keq4D1JGlY9tken4LLXKi2'),
       (3, 'Guest', 'Admin', '5121231235', 'admin@texastoc.com',
        '$2a$10$RRxUYMgQJu99pCMsny6UP.b8I7pheP5Keq4D1JGlY9tken4LLXKi2');
INSERT INTO role
VALUES (1, 'ADMIN', 1),
       (2, 'USER', 1),
       (3, 'USER', 2),
       (4, 'ADMIN', 3),
       (5, 'USER', 3);
