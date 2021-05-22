ALTER TABLE game_player
    MODIFY COLUMN buy_in_collected int;
ALTER TABLE game_player
    MODIFY COLUMN rebuy_add_on_collected int;
ALTER TABLE game_player
    MODIFY COLUMN annual_toc_collected int;
ALTER TABLE game_player
    MODIFY COLUMN quarterly_toc_collected int;