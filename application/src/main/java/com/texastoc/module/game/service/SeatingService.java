package com.texastoc.module.game.service;

import com.texastoc.exception.BLException;
import com.texastoc.exception.BLType;
import com.texastoc.exception.ErrorDetails;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.model.GameTable;
import com.texastoc.module.game.model.Seat;
import com.texastoc.module.game.model.Seating;
import com.texastoc.module.game.model.SeatsPerTable;
import com.texastoc.module.game.model.TableRequest;
import com.texastoc.module.game.repository.GameRepository;
import com.texastoc.module.notification.NotificationModule;
import com.texastoc.module.notification.NotificationModuleFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeatingService {

  private final GameRepository gameRepository;
  private NotificationModule notificationModule;

  private final Random random = new Random(System.currentTimeMillis());

  public SeatingService(GameRepository gameRepository) {
    this.gameRepository = gameRepository;
  }

  @Transactional
  public Seating seatGamePlayers(Seating seating) {
    Optional<Game> optionalGame = gameRepository.findById(seating.getGameId());
    if (!optionalGame.isPresent()) {
      throw new BLException(BLType.NOT_FOUND, ErrorDetails.builder()
          .target("game")
          .message("with id '" + seating.getGameId() + "' not found")
          .build());
    }
    Game game = optionalGame.get();
    game.setSeating(seating);

    if (seating.getSeatsPerTables() == null) {
      seating.setSeatsPerTables(Collections.emptyList());
    }
    if (seating.getTableRequests() == null) {
      seating.setTableRequests(Collections.emptyList());
    }

    // Set the name of the player requesting a table
    seating.getTableRequests().forEach(tableRequest -> {
      if (tableRequest.getGamePlayerName() == null) {
        for (GamePlayer gp : game.getPlayers()) {
          if (tableRequest.getGamePlayerId() == gp.getId()) {
            tableRequest.setGamePlayerName(gp.getName());
            break;
          }
        }
      }
    });

    int numTables = seating.getSeatsPerTables().size();
    List<GameTable> gameTables = new ArrayList<>(numTables);
    seating.setGameTables(gameTables);

    if (numTables == 0) {
      gameRepository.save(game);
      return seating;
    }

    List<GamePlayer> currentPlayers = game.getPlayers();
    // Players that are in the game and have a buy in
    List<GamePlayer> playersWithBuyin = new ArrayList<>(currentPlayers.size());
    for (GamePlayer gamePlayer : currentPlayers) {
      if (gamePlayer.isBoughtIn()) {
        playersWithBuyin.add(gamePlayer);
      }
    }

    if (playersWithBuyin.size() > 2) {
      // shuffle the players
      Random random = new Random(System.currentTimeMillis());
      for (int loop = 0; loop < 10; ++loop) {
        Collections.shuffle(playersWithBuyin, random);
      }
    }

    // Create the seats for the tables (tables are numbered 1's based)
    for (int i = 1; i <= numTables; i++) {
      GameTable gameTable = GameTable.builder()
          .tableNum(i)
          .build();
      gameTables.add(gameTable);
      // Get the seats per table for table
      final int tableNum = i;
      SeatsPerTable seatsPerTable = seating.getSeatsPerTables().stream()
          .filter(spt -> spt.getTableNum() == tableNum)
          .findFirst().get();
      List<Seat> seats = new ArrayList<>(seatsPerTable.getNumSeats());
      // All seats are dead stacks
      for (int j = 0; j < seatsPerTable.getNumSeats(); j++) {
        seats.add(null);
      }
      gameTable.setSeats(seats);
    }

    int totalPlayersRemaining = playersWithBuyin.size();
    while ((totalPlayersRemaining) > 0) {
      // Add players in order
      for (GameTable gameTable : gameTables) {
        if (totalPlayersRemaining == 0) {
          break;
        }
        // Get a player
        GamePlayer gamePlayer = playersWithBuyin.get(--totalPlayersRemaining);

        // Put the player at an empty seat
        List<Seat> seats = gameTable.getSeats();
        for (int i = 0; i < seats.size(); i++) {
          if (seats.get(i) == null) {
            seats.set(i, Seat.builder()
                .seatNum(i + 1)
                .tableNum(gameTable.getTableNum())
                .gamePlayerId(gamePlayer.getId())
                .gamePlayerName(gamePlayer.getName())
                .build());
            break;
          }
        }
      }
    }

    // Move the players around so there are dead stacks
    for (GameTable table : gameTables) {
      table.setSeats(spacePlayersWithDeadStacks(table.getSeats()));
    }

    // Go through the requests
    for (TableRequest tableRequest : seating.getTableRequests()) {
      // Make sure the table request exists
      final int tableRequestedNum = tableRequest.getTableNum();
      Optional<GameTable> optional = gameTables.stream()
          .filter(gameTable -> gameTable.getTableNum() == tableRequestedNum)
          .findFirst();
      if (!optional.isPresent()) {
        throw new BLException(BLType.CONSTRAINT, ErrorDetails.builder()
            .target("seating.tableRequests.tableNum")
            .message("for '" + tableRequestedNum + "' is not valid")
            .build());
      }

      // Find the seat of the player that wants to swap
      Seat playerThatWantsToSwapSeat = null;
      tableLoop:
      for (GameTable table : gameTables) {
        for (Seat seat : table.getSeats()) {
          if (seat != null && seat.getGamePlayerId() != null
              && seat.getGamePlayerId() == tableRequest.getGamePlayerId()) {
            playerThatWantsToSwapSeat = seat;
            break tableLoop;
          }
        }
      }

      if (playerThatWantsToSwapSeat == null) {
        // should never happen
        continue;
      }

      GameTable tableToMoveTo = gameTables.get(tableRequest.getTableNum() - 1);

      // See if player is already at that table
      if (playerThatWantsToSwapSeat.getTableNum() != tableToMoveTo.getTableNum()) {
        for (Seat seatAtTableToMoveTo : tableToMoveTo.getSeats()) {
          // Find a seat to swap - avoid seats that are dead stacks and avoid seats of players that have already been swapped
          if (seatAtTableToMoveTo != null && seatAtTableToMoveTo.getGamePlayerId() != null
              && !seatBelongsToPlayerThatRequestedTheTable(seatAtTableToMoveTo.getGamePlayerId(),
              seating.getTableRequests())) {
            // Swap
            int saveGamePlayerId = seatAtTableToMoveTo.getGamePlayerId();
            String saveGamePlayerName = seatAtTableToMoveTo.getGamePlayerName();

            seatAtTableToMoveTo.setGamePlayerId(playerThatWantsToSwapSeat.getGamePlayerId());
            seatAtTableToMoveTo.setGamePlayerName(playerThatWantsToSwapSeat.getGamePlayerName());

            playerThatWantsToSwapSeat.setGamePlayerId(saveGamePlayerId);
            playerThatWantsToSwapSeat.setGamePlayerName(saveGamePlayerName);
            break;
          }
        }
      }
    }

    // Remove the null seats
    for (GameTable gameTable : seating.getGameTables()) {
      gameTable.setSeats(gameTable.getSeats().stream()
          .filter(Objects::nonNull)
          .collect(Collectors.toList()));
    }

    gameRepository.save(game);
    return seating;
  }

  private List<Seat> spacePlayersWithDeadStacks(List<Seat> seats) {
    // Some sanity checking
    if (seats == null || seats.size() == 0 || seats.get(0) == null) {
      return seats;
    }

    int numSeats = seats.size();
    int numPlayers = (int) seats.stream()
        .filter(seat -> seat != null)
        .count();

    if (numPlayers == numSeats || numPlayers == (numSeats - 1)) {
      return seats;
    }

    int numDeadStacks = numSeats - numPlayers;

    if (numPlayers >= numDeadStacks) {
      // The number of dead stacks do not outnumber the players. Alternate player and dead stack.
      // Currently all the players are seated in the first seats of the table.
      List<Seat> newSeats = new ArrayList<>(numSeats);
      int numDeadStacksSeated = 0;
      int seatNumber = 1;
      for (int i = 0; i < seats.size(); i++) {
        Seat seat = seats.get(i);
        if (seat != null) {
          // Found a seat with a player in it
          seat.setSeatNum(seatNumber++);
          newSeats.add(seat);
          // Now decide if the next seat should be a dead stack
          if (numDeadStacksSeated < numDeadStacks) {
            // Make the next seat a dead stack
            ++seatNumber;
            ++numDeadStacksSeated;
            newSeats.add(null);
          }
        }
      }
      return newSeats;
    }

    // The number of dead stacks outnumber the players. Put multiple deads stacks between the
    // players. Currently all the players are seated in the first seats of the table.
    List<Seat> newSeats = new ArrayList<>(numSeats);
    final double averageDeadsBetween = numDeadStacks / (double) numPlayers;
    double remainder = 0.0;
    int seatNumber = 1;
    for (int i = 0; i < seats.size(); i++) {
      Seat seat = seats.get(i);
      if (seat != null) {
        seat.setSeatNum(seatNumber++);
        newSeats.add(seat);

        double numDeadsBetween = averageDeadsBetween + remainder;
        int deads = (int) Math.floor(numDeadsBetween);
        for (int j = 0; j < deads; j++) {
          ++seatNumber;
          newSeats.add(null);
        }
        remainder = numDeadsBetween - deads;
      }
    }
    return newSeats;
  }

  private boolean seatBelongsToPlayerThatRequestedTheTable(int gamePlayerId,
      List<TableRequest> tableRequests) {
    for (TableRequest tableRequest : tableRequests) {
      if (tableRequest.getGamePlayerId() == gamePlayerId) {
        return true;
      }
    }
    return false;
  }

  public void notifySeating(int gameId) {
    Game game = gameRepository.findById(gameId).get();
    Seating seating = game.getSeating();
    if (seating == null || seating.getGameTables() == null || seating.getGameTables().size() == 0) {
      return;
    }
    for (GameTable table : seating.getGameTables()) {
      if (table.getSeats() == null || table.getSeats().size() == 0) {
        continue;
      }
      for (Seat seat : table.getSeats()) {
        if (seat == null) {
          continue;
        }
        GamePlayer gamePlayer = game.getPlayers().stream()
            .filter(gp -> gp.getId() == seat.getGamePlayerId())
            .findFirst().get();

        if (gamePlayer.getPhone() != null) {
          getNotificaionModule().sendText(gamePlayer.getPhone(), gamePlayer.getName() + " table " +
              table.getTableNum() + " seat " + seat.getSeatNum());
        }
      }
    }
  }

  private NotificationModule getNotificaionModule() {
    if (notificationModule == null) {
      notificationModule = NotificationModuleFactory.getNotificationModule();
    }
    return notificationModule;
  }
}
