package com.texastoc.module.season;

import com.texastoc.module.season.exception.GameInProgressException;
import com.texastoc.module.season.exception.SeasonInProgressException;
import com.texastoc.module.season.model.HistoricalSeason;
import com.texastoc.module.season.model.Season;
import com.texastoc.module.season.service.HistoricalSeasonService;
import com.texastoc.module.season.service.SeasonService;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SeasonRestController {

  private final SeasonService seasonService;
  private final HistoricalSeasonService historicalSeasonService;

  public SeasonRestController(SeasonService seasonService,
      HistoricalSeasonService historicalSeasonService) {
    this.seasonService = seasonService;
    this.historicalSeasonService = historicalSeasonService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/api/v3/seasons")
  public Season createSeason(@RequestBody SeasonStart seasonStart) {
    return seasonService.create(seasonStart.getStartYear());
  }

  @GetMapping("/api/v3/seasons/{id}")
  public Season getSeason(@PathVariable("id") int id) {
    return seasonService.get(id);
  }

  @GetMapping("/api/v3/seasons")
  public List<Season> getSeasons() {
    return seasonService.getAll();
  }

  @GetMapping("/api/v3/seasons/current")
  public Season getCurrentSeason() {
    int id = seasonService.getCurrent().getId();
    return seasonService.get(id);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(value = "/api/v3/seasons/{id}", consumes = "application/vnd.texastoc.finalize+json")
  public Season finalizeSeason(@PathVariable("id") int id) {
    return seasonService.end(id);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(value = "/api/v3/seasons/{id}", consumes = "application/vnd.texastoc.unfinalize+json")
  public Season unfinalizeSeason(@PathVariable("id") int id) {
    return seasonService.open(id);
  }

  @GetMapping("/api/v3/seasons/history")
  public List<HistoricalSeason> getPastSeasons() {
    return historicalSeasonService.getPastSeasons();
  }

  @ExceptionHandler(value = {GameInProgressException.class})
  protected void handleGameInProgressException(GameInProgressException ex,
      HttpServletResponse response) throws IOException {
    response.sendError(HttpStatus.CONFLICT.value(), ex.getMessage());
  }

  @ExceptionHandler(value = {SeasonInProgressException.class})
  protected void handleSeasonInProgressException(SeasonInProgressException ex,
      HttpServletResponse response) throws IOException {
    String message = ex.getMessage();
    System.out.println(message);
    response.sendError(HttpStatus.CONFLICT.value(), ex.getMessage());
  }

  private static class SeasonStart {

    private int startYear;

    public int getStartYear() {
      return startYear;
    }

    public void setStartYear(int startYear) {
      this.startYear = startYear;
    }
  }
}
