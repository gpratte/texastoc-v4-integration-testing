package com.texastoc.module.season;

import com.texastoc.module.season.model.HistoricalSeason;
import com.texastoc.module.season.model.Season;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v4")
public class SeasonRestController {

  private final SeasonModuleImpl seasonModule;

  public SeasonRestController(SeasonModuleImpl seasonModule) {
    this.seasonModule = seasonModule;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/seasons")
  @ResponseStatus(HttpStatus.CREATED)
  public Season createSeason(@RequestBody SeasonStart seasonStart) {
    return seasonModule.create(seasonStart.getStartYear());
  }

  @GetMapping("/seasons/{id}")
  @ResponseStatus(HttpStatus.OK)
  public Season getSeason(@PathVariable("id") int id) {
    return seasonModule.get(id);
  }

  @GetMapping("/seasons")
  @ResponseStatus(HttpStatus.OK)
  public List<Season> getSeasons(HttpServletRequest request) {
    return seasonModule.getAll();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(value = "/seasons/{id}", consumes = "application/vnd.texastoc.finalize+json")
  @ResponseStatus(HttpStatus.OK)
  public Season finalizeSeason(@PathVariable("id") int id) {
    return seasonModule.end(id);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(value = "/seasons/{id}", consumes = "application/vnd.texastoc.unfinalize+json")
  @ResponseStatus(HttpStatus.OK)
  public Season unfinalizeSeason(@PathVariable("id") int id) {
    return seasonModule.open(id);
  }

  @GetMapping("/seasons/history")
  @ResponseStatus(HttpStatus.OK)
  public List<HistoricalSeason> getPastSeasons(HttpServletRequest request) {
    return seasonModule.getPastSeasons();
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
