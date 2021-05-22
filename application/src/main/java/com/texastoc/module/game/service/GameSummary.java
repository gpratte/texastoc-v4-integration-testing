package com.texastoc.module.game.service;

import com.texastoc.module.game.model.Game;
import com.texastoc.module.notification.NotificationModule;
import com.texastoc.module.notification.NotificationModuleFactory;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import com.texastoc.module.quarterly.model.QuarterlySeason;
import com.texastoc.module.season.model.Season;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

public class GameSummary implements Runnable {

  private Game game;
  private Season season;
  private List<QuarterlySeason> quarterlySeasons;
  private List<Player> players;

  private NotificationModule notificationModule;

  public GameSummary(Game game, Season season, List<QuarterlySeason> quarterlySeasons,
      List<Player> players) {
    this.game = game;
    this.season = season;
    this.quarterlySeasons = quarterlySeasons;
    this.players = players;
  }

  @Override
  public void run() {
    String body = getGameSummaryFromTemplate(game, season);
    String subject = "Summary " + game.getDate();

    List<String> emails = new LinkedList<>();
    for (Player player : players) {
      boolean isAdmin = false;
      for (Role role : player.getRoles()) {
        if (Role.Type.ADMIN == role.getType()) {
          isAdmin = true;
          break;
        }
      }

      if (isAdmin && !StringUtils.isBlank(player.getEmail())) {
        emails.add(player.getEmail());
      }
    }
    getNotificationModule().sendEmail(emails, subject, body);
  }

  private static final VelocityEngine VELOCITY_ENGINE = new VelocityEngine();

  static {
    VELOCITY_ENGINE.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
    VELOCITY_ENGINE
        .setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    VELOCITY_ENGINE.init();
  }

  private String getGameSummaryFromTemplate(Game game, Season season) {
    Template t = VELOCITY_ENGINE.getTemplate("game-summary.vm");
    VelocityContext context = new VelocityContext();

    context.put("game", game);

    context.put("season", season);

    QuarterlySeason currentQSeason = quarterlySeasons.stream()
        .filter(
            qs -> !game.getDate().isAfter(qs.getEnd()) && !game.getDate().isBefore(qs.getStart()))
        .findFirst().get();
    context.put("qSeason", currentQSeason);

    StringWriter writer = new StringWriter();
    t.merge(context, writer);
    return writer.toString();
  }

  private NotificationModule getNotificationModule() {
    if (notificationModule == null) {
      notificationModule = NotificationModuleFactory.getNotificationModule();
    }
    return notificationModule;
  }

}
