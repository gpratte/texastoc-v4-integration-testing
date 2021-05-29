package com.texastoc.module.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.texastoc.BaseIntegrationTest;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.model.GameTable;
import com.texastoc.module.game.model.Seat;
import com.texastoc.module.game.model.Seating;
import com.texastoc.module.game.model.SeatsPerTable;
import com.texastoc.module.game.model.TableRequest;
import com.texastoc.module.season.model.Season;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.HttpClientErrorException;

public class GameSeatingIT extends BaseIntegrationTest {

  static final Random RANDOM = new Random(System.currentTimeMillis());

  Integer gameId;
  Season seasonCreated;
  Seating seating;
  List<GamePlayer> gamePlayers;
  HttpClientErrorException exception;

  @Before
  public void before() {
    gameId = null;
    seasonCreated = null;
    seating = null;
    gamePlayers = new LinkedList<>();
    exception = null;
  }

  /**
   * Seat 0 players at 1 table with no table requests
   */
  @Test
  public void zeroAtOneNoRequests() throws Exception {
    aGameHasGamePlayers(0);
    seatingIsDoneWithTableAndSeatsAndRequests(1, 10, 0, 0);
    gamePlayersAreSeatedAtTable(0, 1);
    tableHasDeadStacks(1, 10);
  }

  /**
   * Seat 9 players at table 1 with no table requests
   */
  @Test
  public void nineAtOneNoRequests() throws Exception {
    aGameHasGamePlayers(9);
    seatingIsDoneWithTableAndSeatsAndRequests(1, 9, 0, 0);
    gamePlayersAreSeatedAtTable(9, 1);
    tableHasDeadStacks(1, 0);
  }

  /**
   * Seat 13 players at 2 tables with no table requests
   */
  @Test
  public void thirteenAtTwoNoRequests() throws Exception {
    aGameHasGamePlayers(13);
    seatingIsDoneWithTableAndSeatsAndRequests(2, 8, 0, 0);
    gamePlayersAreSeatedAtTable(7, 1);
    gamePlayersAreSeatedAtTable(6, 2);
    tableHasDeadStacks(1, 1);
    tableHasDeadStacks(2, 2);
  }

  /**
   * Seat 40 players at 4 tables with 2 table requests to move to table 3
   */
  @Test
  public void fortyAtFourTwoRequests() throws Exception {
    aGameHasGamePlayers(40);
    seatingIsDoneWithTableAndSeatsAndRequests(4, 10, 2, 3);
    requestsAreFulfilled();
  }

  /**
   * Seat 2 players at 1 tables with 1 table requests to move to table 3
   */
  @Test
  public void twoAtOneOneRequests() throws Exception {
    aGameHasGamePlayers(2);
    seatingIsDoneWithTableAndSeatsAndRequests(1, 10, 1, 3);
    invalidSeating();
  }

  private void aGameHasGamePlayers(int numPlayers) throws Exception {
    String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    seasonCreated = createSeason(getSeasonStart().getYear(), token);

    Game game = createGame(Game.builder()
        .date(LocalDate.now())
        .hostId(1)
        .transportRequired(false)
        .build(), seasonCreated.getId(), token);

    gameId = game.getId();

    token = login(USER_EMAIL, USER_PASSWORD);

    for (int i = 0; i < numPlayers; i++) {
      GamePlayer gamePlayer = GamePlayer.builder()
          .firstName("Joe" + i)
          .lastName("Schmoe")
          .email("joe" + UUID.randomUUID() + ".schmoe@texastoc.com")
          .gameId(gameId)
          .boughtIn(true)
          .build();
      gamePlayers.add(addFirstTimePlayerToGame(gamePlayer, token));
    }
  }

  private void seatingIsDoneWithTableAndSeatsAndRequests(int tables, int seats, int requests,
      int tableRequested) throws Exception {
    String token = login(USER_EMAIL, USER_PASSWORD);
    Seating seatingRequest = new Seating();
    List<SeatsPerTable> seatsPerTables = new ArrayList<>(tables);
    seatingRequest.setSeatsPerTables(seatsPerTables);
    for (int i = 0; i < tables; i++) {
      SeatsPerTable seatsPerTable = new SeatsPerTable();
      seatsPerTables.add(seatsPerTable);
      seatsPerTable.setTableNum(i + 1);
      seatsPerTable.setNumSeats(seats);
    }

    if (requests > 0) {
      List<TableRequest> tableRequests = new ArrayList<>(requests);
      List<GamePlayer> gamePlayersThatHaveRequested = new LinkedList<>();
      seatingRequest.setTableRequests(tableRequests);
      for (int i = 0; i < requests; i++) {
        TableRequest tableRequest = new TableRequest();
        tableRequests.add(tableRequest);
        tableRequest.setTableNum(tableRequested);
        // Randomly pick a player
        GamePlayer gamePlayer = null;
        do {
          int index = RANDOM.nextInt(gamePlayers.size());
          gamePlayer = gamePlayers.get(index);
          if (!gamePlayersThatHaveRequested.contains(gamePlayer)) {
            gamePlayersThatHaveRequested.add(gamePlayer);
            break;
          }
        } while (true);
        tableRequest.setGamePlayerId(gamePlayer.getId());
        tableRequest.setGamePlayerName(gamePlayer.getName());
      }
    }

    try {
      seating = seatPlayers(gameId, seatingRequest, token);
    } catch (HttpClientErrorException e) {
      exception = e;
    }
  }

  private void gamePlayersAreSeatedAtTable(int numPlayers, int tableNum) throws Exception {
    List<GameTable> gameTables = seating.getGameTables();
    assertNotNull("tables should not be null", gameTables);

    GameTable gameTable = gameTables.get(tableNum - 1);
    assertNotNull("seats should not be null", gameTable.getSeats());

    List<Seat> seats = gameTable.getSeats();
    int numPlayersSeated = (int) seats.stream()
        .filter((seat) -> seat != null)
        .count();
    assertEquals(numPlayers + " are seated", numPlayers, numPlayersSeated);
  }

  private void tableHasDeadStacks(int tableNum, int numDeadStacks) throws Exception {
    List<GameTable> gameTables = seating.getGameTables();
    GameTable table = gameTables.get(tableNum - 1);
    List<Seat> seats = table.getSeats();
    int numPlayersSeated = (int) seats.stream()
        .filter((seat) -> seat != null)
        .count();

    SeatsPerTable seatsPerTable = seating.getSeatsPerTables().get(tableNum - 1);
    int numEmptySeats = seatsPerTable.getNumSeats() - numPlayersSeated;
    assertEquals("should have " + numDeadStacks + " dead stacks", numDeadStacks, numEmptySeats);
  }

  private void requestsAreFulfilled() throws Exception {
    List<GameTable> gameTables = seating.getGameTables();
    List<TableRequest> tableRequests = seating.getTableRequests();

    for (TableRequest tableRequest : tableRequests) {
      int tableRequested = tableRequest.getTableNum();
      int playerIdRequesting = tableRequest.getGamePlayerId();

      boolean found = false;
      gameTableLoop:
      for (GameTable gameTable : gameTables) {
        if (gameTable.getTableNum() == tableRequested) {
          for (Seat seat : gameTable.getSeats()) {
            if (playerIdRequesting == seat.getGamePlayerId()) {
              found = true;
              break gameTableLoop;
            }
          }
        }
      }
      assertTrue("should have found the player that requested at the table", found);
    }
  }

  private void invalidSeating() throws Exception {
    assertNotNull("exception should have been thrown", exception);
    assertEquals("status should be 400", 400, exception.getStatusCode().value());
  }
}
