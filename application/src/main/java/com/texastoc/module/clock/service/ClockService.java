package com.texastoc.module.clock.service;

import com.texastoc.module.clock.config.RoundsConfig;
import com.texastoc.module.game.GameModule;
import com.texastoc.module.game.GameModuleFactory;
import com.texastoc.module.game.connector.WebSocketConnector;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.model.clock.Clock;
import com.texastoc.module.game.model.clock.Round;
import com.texastoc.module.notification.NotificationModule;
import com.texastoc.module.notification.NotificationModuleFactory;
import com.texastoc.module.player.PlayerModule;
import com.texastoc.module.player.PlayerModuleFactory;
import com.texastoc.module.player.model.Player;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ClockService {

  private final WebSocketConnector webSocketConnector;
  private final RoundsConfig roundsConfig;

  private GameModule gameModule;
  private NotificationModule notificationModule;

  private final Map<Integer, Clock> clocks = new HashMap<>();
  private final Map<Integer, RunClock> threads = new HashMap<>();

  public ClockService(WebSocketConnector webSocketConnector, RoundsConfig roundsConfig) {
    this.webSocketConnector = webSocketConnector;
    this.roundsConfig = roundsConfig;
  }

  public List<Round> getRounds() {
    return roundsConfig.getRounds();
  }

  public Clock get(int gameId) {
    Clock clock = getClock(gameId);
    int secondsRemaining = (int) (clock.getMillisRemaining() / 1000);
    int minutesRemaining = secondsRemaining / 60;
    secondsRemaining = secondsRemaining - minutesRemaining * 60;
    clock.setMinutes(minutesRemaining);
    clock.setSeconds(secondsRemaining);
    return clock;
  }

  public void resume(int gameId) {
    Clock clock = getClock(gameId);
    clock.setPlaying(true);

    if (!threads.containsKey(gameId)) {
      RunClock runClock = new RunClock(gameId, clock);
      new Thread(runClock).start();
      threads.put(gameId, runClock);
    }
  }

  public void pause(int gameId) {
    Clock clock = getClock(gameId);
    clock.setPlaying(false);
    updateListeners(gameId);
  }

  public void back(int gameId) {
    Clock clock = getClock(gameId);
    if (clock.getMillisRemaining() == 0) {
      // Move to previous round
      Round thisRound = findPreviousRound(clock.getThisRound());
      clock.setThisRound(thisRound);
      clock.setNextRound(findNextRound(thisRound));
      clock.setMillisRemaining(thisRound.getDuration() * 60 * 1000);
      notifyRoundChange(gameId);
      updateListeners(gameId);
      return;
    }

    long millisRemaining = clock.getMillisRemaining() - 60000;
    if (millisRemaining < 0) {
      millisRemaining = 0;
    }
    clock.setMillisRemaining(millisRemaining);
    updateListeners(gameId);
  }

  public void forward(int gameId) {
    Clock clock = getClock(gameId);
    // If already at max time go to next round
    if (clock.getMillisRemaining() == (clock.getThisRound().getDuration() * 60 * 1000)) {
      // Move to next round
      Round thisRound = clock.getNextRound();
      clock.setThisRound(thisRound);
      clock.setNextRound(findNextRound(clock.getThisRound()));
      clock.setMillisRemaining(thisRound.getDuration() * 60 * 1000);
      notifyRoundChange(gameId);
      updateListeners(gameId);
      return;
    }

    // add a minute
    long millisRemaining = clock.getMillisRemaining() + 60000;

    // Make sure not over maximum time
    if (millisRemaining > (clock.getThisRound().getDuration() * 60 * 1000)) {
      millisRemaining = clock.getThisRound().getDuration() * 60 * 1000;
    }
    clock.setMillisRemaining(millisRemaining);
    updateListeners(gameId);
  }

  public void endClock(int gameId) {
    RunClock runClock = threads.get(gameId);
    if (runClock != null) {
      runClock.endClock();
      threads.remove(gameId);
    }
    Clock clock = clocks.get(gameId);
    if (clock != null) {
      clock.setPlaying(false);
    }
  }

  private Clock getClock(int gameId) {
    Clock clock = clocks.get(gameId);
    if (clock == null) {
      Round round1 = roundsConfig.getRounds().get(0);
      clock = Clock.builder()
          .gameId(gameId)
          .minutes(round1.getDuration())
          .seconds(0)
          .playing(false)
          .thisRound(round1)
          .nextRound(roundsConfig.getRounds().get(1))
          .millisRemaining(round1.getDuration() * 60 * 1000)
          .build();
      clocks.put(gameId, clock);
    }
    return clock;
  }

  private Round findPreviousRound(Round round) {
    for (int i = roundsConfig.getRounds().size() - 1; i >= 0; i--) {
      if (i == 0) {
        // return first round
        return roundsConfig.getRounds().get(0);
      }
      if (round.getName().equals(roundsConfig.getRounds().get(i).getName())) {
        return roundsConfig.getRounds().get(i - 1);
      }
    }
    // should never get here
    return null;
  }

  private Round findNextRound(Round round) {
    for (int i = 0; i < roundsConfig.getRounds().size(); i++) {
      if (i == roundsConfig.getRounds().size() - 1) {
        // last round repeats
        return roundsConfig.getRounds().get(roundsConfig.getRounds().size() - 1);
      }
      if (round.getName().equals(roundsConfig.getRounds().get(i).getName())) {
        return roundsConfig.getRounds().get(i + 1);
      }
    }
    // should never get here
    return null;
  }

  private void notifyRoundChange(int gameId) {
    Clock clock = clocks.get(gameId);
    Game game = getGameModule().get(gameId);
    List<GamePlayer> gamePlayers = game.getPlayers();
    PlayerModule playerModule = PlayerModuleFactory.getPlayerModule();
    gamePlayers.forEach((gp) -> {
      if (gp.isRoundUpdates()) {
        Player player = playerModule.get(gp.getPlayerId());
        if (player.getPhone() != null) {
          getNotificationModule().sendText(player.getPhone(), clock.getThisRound().getName());
        }
      }
    });
    Boolean canRebuy = null;
    switch (clock.getThisRound().getName()) {
      case "Round 1":
      case "Round 2":
      case "Round 3":
      case "Round 4":
      case "Round 5":
      case "Round 6":
      case "Round 7":
      case "Break 1":
        canRebuy = true;
        break;
      default:
        canRebuy = false;
    }
    // TODO need to message this instead
    getGameModule().updateCanRebuy(gameId, canRebuy);
  }

  private void updateListeners(int gameId) {
    webSocketConnector.sendClock(get(gameId));
  }

  private GameModule getGameModule() {
    if (gameModule == null) {
      gameModule = GameModuleFactory.getGameModule();
    }
    return gameModule;
  }

  private NotificationModule getNotificationModule() {
    if (notificationModule == null) {
      notificationModule = NotificationModuleFactory.getNotificationModule();
    }
    return notificationModule;
  }

  class RunClock implements Runnable {

    private final int gameId;
    private final Clock clock;
    private boolean end = false;
    private int lastRoundRepeated = 0;

    public RunClock(int gameId, Clock clock) {
      this.gameId = gameId;
      this.clock = clock;
    }

    public void endClock() {
      end = true;
    }

    @Override
    public void run() {
      while (!end) {
        if (clock.isPlaying()) {
          if (clock.getMillisRemaining() > 0) {
            // current round is running
            long start = System.currentTimeMillis();
            try {
              Thread.sleep(900l);
            } catch (InterruptedException e) {
              // Do nothing
            }
            long clockRan = System.currentTimeMillis() - start;
            System.out.print(". ");
            clock.setMillisRemaining(clock.getMillisRemaining() - clockRan);
          } else {
            // Move to next round
            Round thisRound = clock.getNextRound();
            clock.setThisRound(thisRound);
            clock.setNextRound(findNextRound(clock.getThisRound()));
            clock.setMillisRemaining(thisRound.getDuration() * 60 * 1000);
            notifyRoundChange(gameId);

            // Check if the last round has been repeated 10 times. If so then end
            if (clock.getThisRound().getName().equals(clock.getNextRound().getName())) {
              if (++lastRoundRepeated == 10) {
                end = true;
              }
            }
          }
          updateListeners(gameId);
        } else {
          // Sleep while not playing
          // TODO use thread notify instead
          try {
            Thread.sleep(1000l);
          } catch (InterruptedException e) {
            // Do nothing
          }
        }
      }
    }
  }
}
