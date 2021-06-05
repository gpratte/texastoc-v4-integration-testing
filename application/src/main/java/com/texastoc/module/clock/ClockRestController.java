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

  public static final String CONTENT_TYPE_RESUME = "application/vnd.texastoc.clock-resume+json";
  public static final String CONTENT_TYPE_PAUSE = "application/vnd.texastoc.clock-pause+json";
  public static final String CONTENT_TYPE_BACK = "application/vnd.texastoc.clock-back+json";
  public static final String CONTENT_TYPE_STEP_BACK = "application/vnd.texastoc.clock-step-back+json";
  public static final String CONTENT_TYPE_FORWARD = "application/vnd.texastoc.clock-forward+json";
  public static final String CONTENT_TYPE_STEP_FORWARD = "application/vnd.texastoc.clock-step-forward+json";

  private final ClockService clockService;

  public ClockRestController(ClockService clockService) {
    this.clockService = clockService;
  }

  @GetMapping("/games/{id}/clock")
  public Clock getClock(@PathVariable("id") int id) {
    return clockService.get(id);
  }

  @PostMapping(value = "/games/{id}/clock", consumes = CONTENT_TYPE_RESUME)
  public void resume(@PathVariable("id") int id) {
    clockService.resume(id);
  }

  @PostMapping(value = "/games/{id}/clock", consumes = CONTENT_TYPE_PAUSE)
  public void pause(@PathVariable("id") int id) {
    clockService.pause(id);
  }

  @PostMapping(value = "/games/{id}/clock", consumes = CONTENT_TYPE_BACK)
  public void back(@PathVariable("id") int id) {
    clockService.back(id);
  }

  @PostMapping(value = "/games/{id}/clock", consumes = CONTENT_TYPE_STEP_BACK)
  public void stepBack(@PathVariable("id") int id) {
    clockService.stepBack(id);
  }

  @PostMapping(value = "/games/{id}/clock", consumes = CONTENT_TYPE_FORWARD)
  public void forward(@PathVariable("id") int id) {
    clockService.forward(id);
  }

  @PostMapping(value = "/games/{id}/clock", consumes = CONTENT_TYPE_STEP_FORWARD)
  public void stepForward(@PathVariable("id") int id) {
    clockService.stepForward(id);
  }

  @GetMapping("/clock/rounds")
  public List<Round> getRounds() {
    return clockService.getRounds();
  }

}
