package com.texastoc.module.season.repository;

import com.texastoc.module.season.model.HistoricalSeason;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeasonHistoryRepository extends CrudRepository<HistoricalSeason, String> {

  List<HistoricalSeason> findByOrderByStartYearDesc();

}
