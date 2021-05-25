package com.texastoc.module.season.repository;

import com.texastoc.module.season.model.Season;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeasonRepository extends CrudRepository<Season, Integer> {

}
