package com.texastoc.module.clock;

import com.texastoc.module.clock.service.ClockService;
import com.texastoc.module.game.model.clock.Clock;
import com.texastoc.module.game.model.clock.Round;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v4")
public class ClockRestController {

  private final ClockService clockService;

  public ClockRestController(ClockService clockService) {
    this.clockService = clockService;
  }

  @GetMapping("/games/{id}/clock")
  public Clock getClock(@PathVariable("id") int id) {
    return clockService.get(id);
  }

  @PostMapping(value = "/games/{id}/clock", consumes = "application/vnd.texastoc.clock-resume+json")
  public void resume(@PathVariable("id") int id) {
    clockService.resume(id);
  }

  @PostMapping(value = "/games/{id}/clock", consumes = "application/vnd.texastoc.clock-pause+json")
  public void pause(@PathVariable("id") int id) {
    clockService.pause(id);
  }

  @PostMapping(value = "/games/{id}/clock", consumes = "application/vnd.texastoc.clock-back+json")
  public void back(@PathVariable("id") int id) {
    clockService.back(id);
  }

  @PostMapping(value = "/games/{id}/clock", consumes = "application/vnd.texastoc.clock-forward+json")
  public void forward(@PathVariable("id") int id) {
    clockService.forward(id);
  }

  @GetMapping("/clock/rounds")
  public List<Round> getRounds() {
    return clockService.getRounds();
  }

}
