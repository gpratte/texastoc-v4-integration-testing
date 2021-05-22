package com.texastoc.module.season.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SeasonPayoutSettings {

  @Id
  private int id;
  private int startYear;

  // JSON
  private String settings;

  // settings JSON deserialized
  @Transient
  private List<SeasonPayoutRange> ranges;

  public List<SeasonPayoutRange> getRanges() {
    if (ranges == null) {
      try {
        ranges = new ObjectMapper()
            .readValue(settings, new TypeReference<List<SeasonPayoutRange>>() {
            });
      } catch (JsonProcessingException e) {
        throw new RuntimeException("problem deserializing settings", e);
      }
    }
    return ranges;
  }
}
