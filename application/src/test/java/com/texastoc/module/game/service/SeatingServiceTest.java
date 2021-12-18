package com.texastoc.module.game.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.texastoc.TestConstants;
import com.texastoc.TestUtils;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class SeatingServiceTest implements TestConstants {

  private SeatingService seatingService;
  private GameRepository gameRepository;
  private NotificationModule notificationModule;

  @Before
  public void init() {
    gameRepository = mock(GameRepository.class);
    seatingService = new SeatingService(gameRepository);

    notificationModule = mock(NotificationModule.class);
    ReflectionTestUtils.setField(seatingService, "notificationModule", notificationModule);
  }

  @Test
  public void seatNoPlayers() {
    // Arrange
    Seating seating = new Seating();
    seating.setGameId(111);

    // 2 players with buy-ins
    List<GamePlayer> gamePlayers = new LinkedList<>();
    for (int i = 0; i < 2; i++) {
      gamePlayers.add(GamePlayer.builder()
          .id(i + 1)
          .boughtIn(true)
          .build());
    }

    Game game = Game.builder()
        .id(111)
        .players(gamePlayers)
        .build();
    when(gameRepository.findById(111)).thenReturn(Optional.of(game));

    // Act
    Seating seated = seatingService.seatGamePlayers(seating);

    // Assert
    assertNotNull(seated.getGameTables());
    assertEquals(0, seated.getGameTables().size());
  }

  @Test
  public void seat8PlayersAt2TablesWith3Request() {
    // Arrange
    List<SeatsPerTable> seatsPerTables = new LinkedList<>();
    SeatsPerTable seatsPerTable = new SeatsPerTable();
    // Table 1 with 6 seats
    seatsPerTable.setTableNum(1);
    seatsPerTable.setNumSeats(6);
    seatsPerTables.add(seatsPerTable);

    seatsPerTable = new SeatsPerTable();
    // Table 2 with 5 seats
    seatsPerTable.setTableNum(2);
    seatsPerTable.setNumSeats(5);
    seatsPerTables.add(seatsPerTable);

    List<TableRequest> tableRequests = new LinkedList<>();
    TableRequest tableRequest = new TableRequest();
    tableRequest.setGamePlayerId(8);
    tableRequest.setGamePlayerName("Twenty Three");
    tableRequest.setTableNum(1);
    tableRequests.add(tableRequest);

    tableRequest = new TableRequest();
    tableRequest.setGamePlayerId(6);
    tableRequest.setGamePlayerName("Twenty Three");
    tableRequest.setTableNum(1);
    tableRequests.add(tableRequest);

    tableRequest = new TableRequest();
    tableRequest.setGamePlayerId(2);
    tableRequest.setGamePlayerName("Twenty Three");
    tableRequest.setTableNum(1);
    tableRequests.add(tableRequest);

    Seating seating = new Seating();
    seating.setGameId(111);
    seating.setSeatsPerTables(seatsPerTables);
    seating.setTableRequests(tableRequests);

    // 8 players with buy-ins
    List<GamePlayer> gamePlayers = new LinkedList<>();
    for (int i = 0; i < 8; i++) {
      gamePlayers.add(GamePlayer.builder()
          .id(i + 1)
          .boughtIn(true)
          .build());
    }
    // 1 players with no buy-in
    gamePlayers.add(GamePlayer.builder()
        .boughtIn(false)
        .build());

    Game game = Game.builder()
        .id(111)
        .players(gamePlayers)
        .build();
    when(gameRepository.findById(111)).thenReturn(Optional.of(game));

    // Act
    Seating seated = seatingService.seatGamePlayers(seating);

    // Assert
    assertNotNull(seated.getGameTables());

    GameTable gameTable1 = null;
    GameTable gameTable2 = null;
    for (GameTable gameTable : seated.getGameTables()) {
      if (gameTable.getTableNum() == 1) {
        gameTable1 = gameTable;
      }
      if (gameTable.getTableNum() == 2) {
        gameTable2 = gameTable;
      }
    }
    assertEquals(4, gameTable1.getSeats().size());
    assertEquals(4, gameTable2.getSeats().size());

    // Find seat for player with id=8, should be at table 1
    Seat seatForPlayerWithId8 = null;
    foundSeatForPlayerWithId8:
    for (GameTable gameTable : seated.getGameTables()) {
      for (Seat seat : gameTable.getSeats()) {
        if (seat.getGamePlayerId() == 8) {
          seatForPlayerWithId8 = seat;
          break foundSeatForPlayerWithId8;
        }
      }
    }
    assertEquals(1, seatForPlayerWithId8.getTableNum());

    // Find seat for player with id=6, should be at table 1
    Seat seatForPlayerWithId6 = null;
    foundSeatForPlayerWithId6:
    for (GameTable gameTable : seated.getGameTables()) {
      for (Seat seat : gameTable.getSeats()) {
        if (seat.getGamePlayerId() == 6) {
          seatForPlayerWithId6 = seat;
          break foundSeatForPlayerWithId6;
        }
      }
    }
    assertEquals(1, seatForPlayerWithId6.getTableNum());

    // Find seat for player with id=2, should be at table 1
    Seat seatForPlayerWithId2 = null;
    foundSeatForPlayerWithId2:
    for (GameTable gameTable : seated.getGameTables()) {
      for (Seat seat : gameTable.getSeats()) {
        if (seat.getGamePlayerId() == 2) {
          seatForPlayerWithId2 = seat;
          break foundSeatForPlayerWithId2;
        }
      }
    }
    assertEquals(1, seatForPlayerWithId2.getTableNum());
  }

  @Test
  public void seat10PlayersAt3Tables() {
    // Arrange
    List<SeatsPerTable> seatsPerTables = new LinkedList<>();
    SeatsPerTable seatsPerTable = new SeatsPerTable();
    // Table 1 with 8 seats
    seatsPerTable.setTableNum(1);
    seatsPerTable.setNumSeats(8);
    seatsPerTables.add(seatsPerTable);

    seatsPerTable = new SeatsPerTable();
    // Table 2 with 8 seats
    seatsPerTable.setTableNum(2);
    seatsPerTable.setNumSeats(8);
    seatsPerTables.add(seatsPerTable);

    seatsPerTable = new SeatsPerTable();
    // Table 3 with 7 seats
    seatsPerTable.setTableNum(3);
    seatsPerTable.setNumSeats(7);
    seatsPerTables.add(seatsPerTable);

    Seating seating = new Seating();
    seating.setGameId(111);
    seating.setSeatsPerTables(seatsPerTables);

    // 10 players with buy-ins
    List<GamePlayer> gamePlayers = new LinkedList<>();
    for (int i = 0; i < 10; i++) {
      gamePlayers.add(GamePlayer.builder()
          .id(i + 1)
          .boughtIn(true)
          .build());
    }
    // 2 players with no buy-in
    gamePlayers.add(GamePlayer.builder()
        .boughtIn(false)
        .build());
    gamePlayers.add(GamePlayer.builder()
        .boughtIn(false)
        .build());

    Game game = Game.builder()
        .id(111)
        .players(gamePlayers)
        .build();
    when(gameRepository.findById(111)).thenReturn(Optional.of(game));

    // Act
    Seating seated = seatingService.seatGamePlayers(seating);

    // Assert
    assertNotNull(seated.getGameTables());

    GameTable gameTable1 = null;
    GameTable gameTable2 = null;
    GameTable gameTable3 = null;
    for (GameTable gameTable : seated.getGameTables()) {
      if (gameTable.getTableNum() == 1) {
        gameTable1 = gameTable;
      }
      if (gameTable.getTableNum() == 2) {
        gameTable2 = gameTable;
      }
      if (gameTable.getTableNum() == 3) {
        gameTable3 = gameTable;
      }
    }
    assertEquals(4, gameTable1.getSeats().size());
    assertEquals(3, gameTable2.getSeats().size());
    assertEquals(3, gameTable3.getSeats().size());
  }

  @Test
  public void seatInvalidTableRequest() {
    // Arrange
    List<SeatsPerTable> seatsPerTables = new LinkedList<>();
    SeatsPerTable seatsPerTable = new SeatsPerTable();
    // Table 1 with 8 seats
    seatsPerTable.setTableNum(1);
    seatsPerTable.setNumSeats(8);
    seatsPerTables.add(seatsPerTable);

    List<TableRequest> tableRequests = new LinkedList<>();
    TableRequest tableRequest = new TableRequest();
    tableRequest.setGamePlayerId(1);
    tableRequest.setGamePlayerName("Twenty Three");
    tableRequest.setTableNum(7);
    tableRequests.add(tableRequest);

    Seating seating = new Seating();
    seating.setGameId(111);
    seating.setSeatsPerTables(seatsPerTables);
    seating.setTableRequests(tableRequests);

    // 2 players with buy-ins
    List<GamePlayer> gamePlayers = new LinkedList<>();
    for (int i = 0; i < 2; i++) {
      gamePlayers.add(GamePlayer.builder()
          .id(i + 1)
          .boughtIn(true)
          .build());
    }

    Game game = Game.builder()
        .id(111)
        .players(gamePlayers)
        .build();
    when(gameRepository.findById(111)).thenReturn(Optional.of(game));

    // Act and Assert
    assertThatThrownBy(() -> {
      seatingService.seatGamePlayers(seating);
    }).isInstanceOf(BLException.class)
        .satisfies(ex -> {
          BLException blException = (BLException) ex;
          TestUtils.verifyBLException(blException, BLType.CONSTRAINT, ErrorDetails.builder()
              .target("seating.tableRequests.tableNum")
              .message("7 is not valid")
              .build());
        });
  }

  @Test
  public void noNotifySeating() {
    // Arrange
    Game game = Game.builder()
        .id(111)
        .build();
    when(gameRepository.findById(111)).thenReturn(Optional.of(game));

    // Act
    seatingService.notifySeating(111);

    // Assert
    Mockito.verify(notificationModule, times(0)).sendText(any(), any());
  }

  @Test
  public void notifySeating() {
    // Arrange
    List<Seat> seats = new ArrayList<>();
    Seat seat = Seat.builder()
        .gamePlayerId(6)
        .build();
    seats.add(seat);

    seats.add(null);

    seat = Seat.builder()
        .gamePlayerId(7)
        .seatNum(2)
        .build();
    seats.add(seat);

    GameTable gameTable = GameTable.builder()
        .tableNum(1)
        .build();
    gameTable.setSeats(seats);

    Seating seating = new Seating();
    seating.setGameId(111);
    seating.setGameTables(Collections.singletonList(gameTable));

    // 2 players with buy-ins
    List<GamePlayer> gamePlayers = new LinkedList<>();
    gamePlayers.add(GamePlayer.builder()
        .id(6)
        .boughtIn(true)
        .build());
    gamePlayers.add(GamePlayer.builder()
        .id(7)
        .boughtIn(true)
        .phone("33344455555")
        .firstName("abe")
        .lastName("abeson")
        .build());

    Game game = Game.builder()
        .id(111)
        .players(gamePlayers)
        .seating(seating)
        .build();
    when(gameRepository.findById(111)).thenReturn(Optional.of(game));

    // Act
    seatingService.notifySeating(111);

    // Assert
    Mockito.verify(notificationModule, times(1))
        .sendText("33344455555", "abe abeson table 1 seat 2");
  }
}
