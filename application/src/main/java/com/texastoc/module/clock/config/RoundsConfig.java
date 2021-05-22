package com.texastoc.module.clock.config;

import com.texastoc.module.game.model.clock.Round;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "clock")
public class RoundsConfig {

  private List<Round> rounds;
}
