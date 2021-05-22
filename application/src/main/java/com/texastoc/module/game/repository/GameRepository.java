package com.texastoc.module.game.repository;

import com.texastoc.module.game.model.Game;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends CrudRepository<Game, Integer> {

  @Query("select * from game where season_id=:seasonId order by game_date desc")
  List<Game> findBySeasonId(@Param("seasonId") int seasonId);

  @Query("select * from game where q_season_id=:qSeasonId")
  List<Game> findByQuarterlySeasonId(@Param("qSeasonId") int qSeasonId);

  @Query("select * from GAME g, GAME_PLAYER gp where g.id = gp.game_id and gp.player_id=:playerId")
  List<Game> findByPlayerId(@Param("playerId") int playerId);

  @Query("select id from game where season_id = :seasonId and finalized = false")
  List<Integer> findUnfinalizedBySeasonId(@Param("seasonId") int seasonId);

  @Query("select id from game where season_id = :seasonId order by game_date desc limit 1")
  List<Integer> findMostRecentBySeasonId(@Param("seasonId") int seasonId);
}
