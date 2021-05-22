package com.texastoc.module.settings.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Settings {

  @Id
  private int id;
  @MappedCollection(idColumn = "ID")
  Version version;
  @MappedCollection(keyColumn = "YEAR")
  private Map<Integer, TocConfig> tocConfigs;
}
