package com.texastoc.module.player.repository;

import com.texastoc.module.player.model.Player;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends CrudRepository<Player, Integer> {

  List<Player> findByEmail(String email);
}
