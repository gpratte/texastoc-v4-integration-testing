DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS player;
DROP TABLE IF EXISTS season;
DROP TABLE IF EXISTS season_player;
DROP TABLE IF EXISTS season_payout;
DROP TABLE IF EXISTS quarterly_season;
DROP TABLE IF EXISTS quarterly_season_player;
DROP TABLE IF EXISTS quarterly_season_payout;
DROP TABLE IF EXISTS seats_per_table;
DROP TABLE IF EXISTS table_request;
DROP TABLE IF EXISTS seat;
DROP TABLE IF EXISTS game_table;
DROP TABLE IF EXISTS game_player;
DROP TABLE IF EXISTS game_payout;
DROP TABLE IF EXISTS seating;
DROP TABLE IF EXISTS game;
DROP TABLE IF EXISTS season_payout_settings;
DROP TABLE IF EXISTS toc_config;
DROP TABLE IF EXISTS settings;
DROP TABLE IF EXISTS version;
DROP TABLE IF EXISTS historical_season_player;
DROP TABLE IF EXISTS historical_season;

CREATE TABLE season
(
    id                                      int NOT NULL AUTO_INCREMENT,
    start                                   date      DEFAULT NULL,
    end                                     date      DEFAULT NULL,
    kitty_per_game_cost                     int       DEFAULT NULL,
    toc_per_game_cost                       int       DEFAULT NULL,
    quarterly_toc_per_game_cost             int       DEFAULT NULL,
    buy_in_cost                             int       DEFAULT NULL,
    rebuy_add_on_cost                       int       DEFAULT NULL,
    rebuy_add_on_toc_debit_cost             int       DEFAULT NULL,
    buy_in_collected                        int       DEFAULT NULL,
    rebuy_add_on_collected                  int       DEFAULT NULL,
    annual_toc_collected                    int       DEFAULT NULL,
    total_collected                         int       DEFAULT NULL,
    annual_toc_from_rebuy_add_on_calculated int       DEFAULT NULL,
    rebuy_add_on_less_annual_toc_calculated int       DEFAULT NULL,
    total_combined_annual_toc_calculated    int       DEFAULT NULL,
    kitty_calculated                        int       DEFAULT NULL,
    prize_pot_calculated                    int       DEFAULT NULL,
    quarterly_num_payouts                   int       DEFAULT NULL,
    num_games                               int       DEFAULT NULL,
    num_games_played                        int       DEFAULT NULL,
    finalized                               boolean   DEFAULT NULL,
    last_calculated                         timestamp DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE season_player
(
    id         int NOT NULL AUTO_INCREMENT,
    player_id  int NOT NULL,
    season_id  int NOT NULL,
    name       varchar(64) DEFAULT NULL,
    entries    int         DEFAULT 0,
    points     int         DEFAULT 0,
    place      int         DEFAULT 0,
    forfeit    boolean     DEFAULT FALSE,
    season     int NOT NULL,
    season_key int NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY Season_Player_Unique (player_id, season_id)
);

CREATE TABLE quarterly_season
(
    id                  int         NOT NULL AUTO_INCREMENT,
    season_id           int         NOT NULL,
    start               date      DEFAULT NULL,
    end                 date      DEFAULT NULL,
    finalized           boolean   DEFAULT NULL,
    quarter             varchar(16) NOT NULL,
    num_games           int       DEFAULT 0,
    num_games_played    int       DEFAULT 0,
    q_toc_collected     int       DEFAULT 0,
    q_toc_per_game_cost int       DEFAULT 0,
    num_payouts         int       DEFAULT 0,
    last_calculated     timestamp DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE quarterly_season_player
(
    id                   int NOT NULL AUTO_INCREMENT,
    player_id            int NOT NULL,
    season_id            int NOT NULL,
    q_season_id          int NOT NULL,
    name                 varchar(64) DEFAULT NULL,
    entries              int         DEFAULT 0,
    points               int         DEFAULT 0,
    place                int         DEFAULT NULL,
    quarterly_season     int NOT NULL,
    quarterly_season_key int NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY QSPlayer_Unique (player_id, season_id, q_season_id)
);

CREATE TABLE quarterly_season_payout
(
    id                   int NOT NULL AUTO_INCREMENT,
    season_id            int NOT NULL,
    q_season_id          int NOT NULL,
    place                int NOT NULL,
    amount               int DEFAULT NULL,
    quarterly_season     int NOT NULL,
    quarterly_season_key int NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY QQSPayout_Unique (season_id, q_season_id, place)
);

CREATE TABLE player
(
    id         int NOT NULL AUTO_INCREMENT,
    first_name varchar(32)  DEFAULT NULL,
    last_name  varchar(32)  DEFAULT NULL,
    phone      varchar(32)  DEFAULT NULL,
    email      varchar(64)  DEFAULT NULL,
    password   varchar(255) DEFAULT NULL,
    PRIMARY KEY (id)
);
ALTER TABLE player
    ADD UNIQUE (email);

CREATE TABLE role
(
    id     int NOT NULL AUTO_INCREMENT,
    type   varchar(255) DEFAULT NULL,
    player int,
    PRIMARY KEY (id)
);
alter table role
    add constraint fk_role_player foreign key (player) references player (id);

CREATE TABLE game
(
    id                                      int  NOT NULL AUTO_INCREMENT,
    host_id                                 int         DEFAULT NULL,
    game_date                               date NOT NULL,
    transport_required                      boolean     DEFAULT FALSE,

    host_name                               varchar(64) DEFAULT NULL,
    season_id                               int  NOT NULL,
    q_season_id                             int  NOT NULL,
    quarter                                 varchar(16) DEFAULT NULL,
    season_game_num                         int         DEFAULT NULL,
    quarterly_game_num                      int         DEFAULT NULL,

    kitty_cost                              int         DEFAULT 0,
    buy_in_cost                             int         DEFAULT 0,
    rebuy_add_on_cost                       int         DEFAULT 0,
    rebuy_add_on_toc_debit_cost             int         DEFAULT 0,
    annual_toc_cost                         int         DEFAULT 0,
    quarterly_toc_cost                      int         DEFAULT 0,

    buy_in_collected                        int         DEFAULT 0,
    rebuy_add_on_collected                  int         DEFAULT 0,
    annual_toc_collected                    int         DEFAULT 0,
    quarterly_toc_collected                 int         DEFAULT 0,
    total_collected                         int         DEFAULT 0,

    annual_toc_from_rebuy_add_on_calculated int         DEFAULT 0,
    rebuy_add_on_less_annual_toc_calculated int         DEFAULT 0,
    total_combined_toc_calculated           int         DEFAULT 0,
    kitty_calculated                        int         DEFAULT 0,
    prize_pot_calculated                    int         DEFAULT 0,

    num_players                             int         DEFAULT 0,
    num_paid_players                        int         DEFAULT 0,
    started                                 timestamp NULL DEFAULT NULL,
    last_calculated                         timestamp   DEFAULT NULL,
    chopped                                 boolean     DEFAULT TRUE,
    can_rebuy                               boolean     DEFAULT TRUE,
    finalized                               boolean     DEFAULT FALSE,
    payout_delta                            int         DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE game_player
(
    id                        int NOT NULL AUTO_INCREMENT,
    player_id                 int NOT NULL,
    game_id                   int NOT NULL,
    bought_in                 boolean     DEFAULT NULL,
    rebought                  boolean     DEFAULT NULL,
    annual_toc_participant    boolean     DEFAULT NULL,
    quarterly_toc_participant boolean     DEFAULT NULL,
    round_updates             boolean     DEFAULT FALSE,
    place                     int         DEFAULT NULL,
    knocked_out               boolean     DEFAULT FALSE,
    chop                      int         DEFAULT NULL,

    season_id                 int NOT NULL,
    q_season_id               int NOT NULL,
    first_name                varchar(64) DEFAULT NULL,
    last_name                 varchar(64) DEFAULT NULL,
    email                     varchar(64) DEFAULT NULL,
    phone                     varchar(64) DEFAULT NULL,
    toc_points                int         DEFAULT NULL,
    toc_chop_points           int         DEFAULT NULL,
    q_toc_points              int         DEFAULT NULL,
    q_toc_chop_points         int         DEFAULT NULL,

    buy_in_collected          int         DEFAULT NULL,
    rebuy_add_on_collected    int         DEFAULT NULL,
    annual_toc_collected      int         DEFAULT NULL,
    quarterly_toc_collected   int         DEFAULT NULL,

    game                      int NOT NULL,
    game_key                  int NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY game_player_unique (game_id, player_id)
);
alter table game_player
    add constraint fk_game_player_game foreign key (game) references game (id);

CREATE TABLE game_payout
(
    id          int NOT NULL AUTO_INCREMENT,
    game_id     int NOT NULL,
    place       int NOT NULL,
    amount      int DEFAULT NULL,
    chop_amount int DEFAULT NULL,
    game        int NOT NULL,
    game_key    int NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY game_payout_unique (game_id, place)
);
alter table game_payout
    add constraint fk_game_payout_game foreign key (game) references game (id);

CREATE TABLE seating
(
    id      int NOT NULL AUTO_INCREMENT,
    game_id int NOT NULL,
    PRIMARY KEY (id)
);
alter table seating
    add constraint fk_seating_game foreign key (game_id) references game (id);

CREATE TABLE seats_per_table
(
    id          int NOT NULL AUTO_INCREMENT,
    num_seats   int NOT NULL,
    table_num   int NOT NULL,
    seating     int NOT NULL,
    seating_key int NOT NULL,
    PRIMARY KEY (id)
);
alter table seats_per_table
    add constraint fk_seats_per_table_seating foreign key (seating) references seating (id);

CREATE TABLE table_request
(
    id               int          NOT NULL AUTO_INCREMENT,
    game_player_id   int          NOT NULL,
    game_player_name varchar(128) NOT NULL,
    table_num        int          NOT NULL,
    seating          int          NOT NULL,
    seating_key      int          NOT NULL,
    PRIMARY KEY (id)
);
alter table table_request
    add constraint fk_table_request_seating foreign key (seating) references seating (id);

CREATE TABLE game_table
(
    id          int NOT NULL AUTO_INCREMENT,
    table_num   int NOT NULL,
    seating     int NOT NULL,
    seating_key int NOT NULL,
    PRIMARY KEY (id)
);
alter table game_table
    add constraint fk_game_table_seating foreign key (seating) references seating (id);

CREATE TABLE seat
(
    id               int NOT NULL AUTO_INCREMENT,
    seat_num         int NOT NULL,
    table_num        int NOT NULL,
    game_player_id   int          DEFAULT NULL,
    game_player_name varchar(128) DEFAULT NULL,
    game_table       int NOT NULL,
    game_table_key   int NOT NULL,
    PRIMARY KEY (id)
);
alter table seat
    add constraint fk_seat_game_table foreign key (game_table) references game_table (id);

CREATE TABLE season_payout
(
    id         int NOT NULL AUTO_INCREMENT,
    season_id  int NOT NULL,
    place      int NOT NULL,
    amount     int     DEFAULT NULL,
    guaranteed boolean DEFAULT false,
    estimated  boolean DEFAULT false,
    cash       boolean DEFAULT false,
    season     int NOT NULL,
    season_key int NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY Season_Payout_Unique (season_id, place, estimated)
);

CREATE TABLE season_payout_settings
(
    id         int           NOT NULL AUTO_INCREMENT,
    start_year int           NOT NULL,
    settings   varchar(8192) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE version
(
    id      int        NOT NULL AUTO_INCREMENT,
    version varchar(8) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE settings
(
    id      int NOT NULL AUTO_INCREMENT,
    version int,
    PRIMARY KEY (id)
);
alter table settings
    add constraint fk_settings_version foreign key (version) references version (id);

CREATE TABLE toc_config
(
    id                      int NOT NULL AUTO_INCREMENT,
    kitty_debit             int NOT NULL,
    annual_toc_cost         int NOT NULL,
    quarterly_toc_cost      int NOT NULL,
    quarterly_num_payouts   int NOT NULL,
    regular_buy_in_cost     int NOT NULL,
    regular_rebuy_cost      int NOT NULL,
    regular_rebuy_toc_debit int NOT NULL,
    year                    int NOT NULL,
    settings                int NOT NULL,
    PRIMARY KEY (id)
);
alter table toc_config
    add constraint fk_toc_config_settings foreign key (settings) references settings (id);

CREATE TABLE historical_season
(
    id                    int NOT NULL AUTO_INCREMENT,
    start_year varchar(8) NOT NULL,
    end_year   varchar(8) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE historical_season_player
(
    id                    int NOT NULL AUTO_INCREMENT,
    name                  varchar(64),
    points                int,
    entries               int,
    start_year            varchar(8) NOT NULL,
    historical_season     int NOT NULL,
    historical_season_key int NOT NULL,
    PRIMARY KEY (id)
);
alter table historical_season_player
    add constraint fk_historical_season_player_historical_season foreign key (historical_season) references historical_season (id);


