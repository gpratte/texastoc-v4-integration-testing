package com.texastoc.module.season.repository;

import com.texastoc.module.season.model.SeasonPayoutSettings;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeasonPayoutSettingsRepository extends
    CrudRepository<SeasonPayoutSettings, Integer> {

  List<SeasonPayoutSettings> findByStartYear(int startYear);
}
