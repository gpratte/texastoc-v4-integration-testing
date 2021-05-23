package com.texastoc.module.quarterly;

import com.texastoc.module.quarterly.model.QuarterlySeason;
import com.texastoc.module.quarterly.service.QuarterlySeasonService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v4")
public class QuarterlySeasonRestController {

  private final QuarterlySeasonService qSeasonService;

  public QuarterlySeasonRestController(
      QuarterlySeasonService qSeasonService) {
    this.qSeasonService = qSeasonService;
  }

  @GetMapping("/seasons/{id}/quarterlies")
  public List<QuarterlySeason> getQuarterlySeasons(@PathVariable("id") int seasonId) {
    return qSeasonService.getBySeasonId(seasonId);
  }

}
