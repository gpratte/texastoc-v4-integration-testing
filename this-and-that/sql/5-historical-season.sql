alter table historical_season drop column season_id;
alter table historical_season_player add column start_year varchar(8) NOT NULL;
alter table historical_season_player
    add constraint fk_historical_season_player_historical_season foreign key (historical_season) references historical_season (id);
