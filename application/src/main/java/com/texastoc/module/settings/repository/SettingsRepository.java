package com.texastoc.module.settings.repository;

import com.texastoc.module.settings.model.Settings;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingsRepository extends CrudRepository<Settings, Integer> {

}
