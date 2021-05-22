package com.texastoc.module.quarterly.repository;

import com.texastoc.module.quarterly.model.QuarterlySeason;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuarterlySeasonRepository extends CrudRepository<QuarterlySeason, Integer> {

  @Query("select * from quarterly_season where :date >= start and :date <= end order by id desc")
  List<QuarterlySeason> findByDate(@Param("date") LocalDate date);

  List<QuarterlySeason> findBySeasonId(@Param("seasonId") int seasonId);

}
