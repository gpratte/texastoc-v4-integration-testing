package com.texastoc.module.season.repository;

import com.texastoc.module.season.model.HistoricalSeason;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeasonHistoryRepository extends CrudRepository<HistoricalSeason, Integer> {

}
