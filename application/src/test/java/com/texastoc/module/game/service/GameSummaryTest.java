package com.texastoc.module.game.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.google.common.collect.ImmutableSet;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePayout;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.notification.NotificationModule;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import com.texastoc.module.quarterly.model.Quarter;
import com.texastoc.module.quarterly.model.QuarterlySeason;
import com.texastoc.module.quarterly.model.QuarterlySeasonPlayer;
import com.texastoc.module.season.model.Season;
import com.texastoc.module.season.model.SeasonPlayer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

public class GameSummaryTest {

  @Test
  public void testGameSummary() throws IOException {
    // Arrange
    Game game = new Game();
    game.setSeasonGameNum(35);
    game.setQuarterlyGameNum(9);
    game.setHostName("Tyrion Lannister");
    LocalDate dec29th = LocalDate.of(2020, 12, 29);
    game.setDate(dec29th);
    game.setNumPaidPlayers(11);
    game.setRebuyAddOnTocDebitCost(20);
    game.setBuyInCollected(440);
    game.setAnnualTocCollected(200);
    game.setQuarterlyTocCollected(100);
    game.setRebuyAddOnLessAnnualTocCalculated(280);
    game.setAnnualTocFromRebuyAddOnCalculated(120);
    game.setPrizePotCalculated(710);
    game.setKittyCalculated(10);
    game.setChopped(true);

    List<GamePayout> gamePayouts = new ArrayList<>(2);
    game.setPayouts(gamePayouts);
    gamePayouts.add(GamePayout.builder()
        .place(1)
        .amount(462)
        .build());
    gamePayouts.add(GamePayout.builder()
        .place(2)
        .amount(248)
        .build());

    List<GamePlayer> gamePlayers = new LinkedList<>();
    game.setPlayers(gamePlayers);
    gamePlayers.add(GamePlayer.builder()
        .firstName("Cersei")
        .lastName("Lannister")
        .place(1)
        .tocPoints(75)
        .chop(10000)
        .buyInCollected(40)
        .rebought(true)
        .rebuyAddOnCollected(40)
        .annualTocParticipant(true)
        .annualTocCollected(20)
        .quarterlyTocParticipant(true)
        .quarterlyTocCollected(20)
        .build());
    gamePlayers.add(GamePlayer.builder()
        .firstName("Daenerys")
        .lastName("Targaryen")
        .place(2)
        .tocPoints(58)
        .chop(7500)
        .buyInCollected(40)
        .rebought(true)
        .rebuyAddOnCollected(40)
        .annualTocParticipant(true)
        .annualTocCollected(20)
        .build());
    gamePlayers.add(GamePlayer.builder()
        .firstName("Jon")
        .lastName("Snow")
        .place(3)
        .buyInCollected(40)
        .rebought(true)
        .rebuyAddOnCollected(40)
        .build());
    gamePlayers.add(GamePlayer.builder()
        .firstName("Sansa")
        .lastName("Stark")
        .place(4)
        .tocPoints(35)
        .buyInCollected(40)
        .rebought(true)
        .rebuyAddOnCollected(40)
        .annualTocParticipant(true)
        .annualTocCollected(20)
        .quarterlyTocParticipant(true)
        .quarterlyTocCollected(20)
        .build());
    gamePlayers.add(GamePlayer.builder()
        .firstName("Arya")
        .lastName("Stark")
        .place(5)
        .tocPoints(27)
        .buyInCollected(40)
        .rebought(true)
        .rebuyAddOnCollected(40)
        .annualTocParticipant(true)
        .annualTocCollected(20)
        .quarterlyTocParticipant(true)
        .quarterlyTocCollected(20)
        .build());
    gamePlayers.add(GamePlayer.builder()
        .firstName("Jaime")
        .lastName("Lannister")
        .place(6)
        .tocPoints(21)
        .buyInCollected(40)
        .rebought(true)
        .rebuyAddOnCollected(40)
        .build());
    gamePlayers.add(GamePlayer.builder()
        .firstName("Jorah")
        .lastName("Mormont")
        .place(7)
        .tocPoints(16)
        .buyInCollected(40)
        .rebought(true)
        .rebuyAddOnCollected(40)
        .annualTocParticipant(true)
        .annualTocCollected(20)
        .build());
    gamePlayers.add(GamePlayer.builder()
        .firstName("Samwell")
        .lastName("Tarly")
        .place(8)
        .buyInCollected(40)
        .rebought(true)
        .rebuyAddOnCollected(40)
        .build());
    gamePlayers.add(GamePlayer.builder()
        .firstName("Theon")
        .lastName("Greyjoy")
        .place(9)
        .tocPoints(10)
        .buyInCollected(40)
        .rebought(true)
        .rebuyAddOnCollected(40)
        .annualTocParticipant(true)
        .annualTocCollected(20)
        .build());
    gamePlayers.add(GamePlayer.builder()
        .firstName("Lord")
        .lastName("Varys")
        .place(10)
        .tocPoints(8)
        .buyInCollected(40)
        .rebought(true)
        .rebuyAddOnCollected(40)
        .annualTocParticipant(true)
        .annualTocCollected(20)
        .quarterlyTocParticipant(true)
        .quarterlyTocCollected(20)
        .build());
    gamePlayers.add(GamePlayer.builder()
        .firstName("Davos")
        .lastName("Seaworth")
        .buyInCollected(40)
        .rebought(true)
        .rebuyAddOnCollected(40)
        .annualTocParticipant(true)
        .annualTocCollected(20)
        .build());

    Season season = new Season();
    season.setNumGamesPlayed(35);
    LocalDate may1st = LocalDate.of(2020, 5, 1);
    season.setStart(may1st);
    LocalDate june30th = LocalDate.of(2021, 4, 30);
    season.setEnd(june30th);
    season.setBuyInCollected(20920);
    season.setRebuyAddOnLessAnnualTocCalculated(13720);
    season.setTotalCombinedAnnualTocCalculated(12360);

    List<SeasonPlayer> seasonPlayers = new LinkedList<>();
    season.setPlayers(seasonPlayers);
    seasonPlayers.add(SeasonPlayer.builder()
        .place(1)
        .name("Theon Greyjoy")
        .points(1160)
        .entries(34)
        .build());
    seasonPlayers.add(SeasonPlayer.builder()
        .place(2)
        .name("Lord Varys")
        .points(1123)
        .entries(35)
        .build());
    seasonPlayers.add(SeasonPlayer.builder()
        .place(3)
        .name("Arya Stark")
        .points(1097)
        .entries(35)
        .build());
    seasonPlayers.add(SeasonPlayer.builder()
        .place(4)
        .name("Bran Stark")
        .points(1055)
        .entries(34)
        .build());
    seasonPlayers.add(SeasonPlayer.builder()
        .place(5)
        .name("Sansa Stark")
        .points(1050)
        .entries(35)
        .build());
    seasonPlayers.add(SeasonPlayer.builder()
        .place(6)
        .name("Daenerys Targaryen")
        .points(989)
        .entries(32)
        .build());
    seasonPlayers.add(SeasonPlayer.builder()
        .place(7)
        .name("Cersei Lannister")
        .points(742)
        .entries(35)
        .build());
    seasonPlayers.add(SeasonPlayer.builder()
        .place(8)
        .name("Jorah Mormont")
        .points(622)
        .entries(28)
        .build());
    seasonPlayers.add(SeasonPlayer.builder()
        .place(9)
        .name("Davos Seaworth")
        .points(554)
        .entries(29)
        .build());
    seasonPlayers.add(SeasonPlayer.builder()
        .place(10)
        .name("Bronn")
        .points(542)
        .entries(20)
        .build());
    seasonPlayers.add(SeasonPlayer.builder()
        .place(11)
        .name("Podrick Payne")
        .points(128)
        .entries(4)
        .build());
    seasonPlayers.add(SeasonPlayer.builder()
        .place(12)
        .name("Grey Worm")
        .points(37)
        .entries(2)
        .build());
    seasonPlayers.add(SeasonPlayer.builder()
        .place(13)
        .name("Eddison Tollett")
        .points(18)
        .entries(1)
        .build());
    seasonPlayers.add(SeasonPlayer.builder()
        .name("Tormund Giantsbane")
        .entries(1)
        .build());
    seasonPlayers.add(SeasonPlayer.builder()
        .name("Barristan Selmy")
        .entries(1)
        .build());

    List<QuarterlySeason> quarterlySeasons = new ArrayList<>(4);
    QuarterlySeason thirdQ = QuarterlySeason.builder()
        .start(LocalDate.of(2020, 11, 1))
        .end(LocalDate.of(2021, 1, 31))
        .quarter(Quarter.THIRD)
        .numGamesPlayed(9)
        .qTocCollected(1060)
        .build();
    quarterlySeasons.add(QuarterlySeason.builder()
        .start(LocalDate.of(2020, 5, 1))
        .end(LocalDate.of(2020, 7, 31))
        .quarter(Quarter.FIRST)
        .build());
    quarterlySeasons.add(QuarterlySeason.builder()
        .start(LocalDate.of(2020, 8, 1))
        .end(LocalDate.of(2020, 10, 31))
        .quarter(Quarter.SECOND)
        .build());
    quarterlySeasons.add(thirdQ);
    quarterlySeasons.add(QuarterlySeason.builder()
        .start(LocalDate.of(2021, 2, 1))
        .end(LocalDate.of(2021, 4, 30))
        .quarter(Quarter.FOURTH)
        .build());

    List<QuarterlySeasonPlayer> qSeasonPlayers = new LinkedList<>();
    thirdQ.setPlayers(qSeasonPlayers);
    qSeasonPlayers.add(QuarterlySeasonPlayer.builder()
        .place(1)
        .name("Jaime Lannister")
        .points(311)
        .entries(9)
        .build());
    qSeasonPlayers.add(QuarterlySeasonPlayer.builder()
        .place(2)
        .name("Cersei Lannister")
        .points(302)
        .entries(9)
        .build());
    qSeasonPlayers.add(QuarterlySeasonPlayer.builder()
        .place(3)
        .name("Sansa Stark")
        .points(281)
        .entries(9)
        .build());
    qSeasonPlayers.add(QuarterlySeasonPlayer.builder()
        .place(4)
        .name("Arya Stark")
        .points(242)
        .entries(9)
        .build());
    qSeasonPlayers.add(QuarterlySeasonPlayer.builder()
        .place(5)
        .name("Bran Stark")
        .points(234)
        .entries(8)
        .build());
    qSeasonPlayers.add(QuarterlySeasonPlayer.builder()
        .place(6)
        .name("Lord Varys")
        .points(166)
        .entries(9)
        .build());

    List<Player> players = new ArrayList<>();
    players.add(Player.builder()
        .email("good-one@texastoc.com")
        .roles(ImmutableSet.of(Role.builder()
            .type(Role.Type.ADMIN)
            .build()))
        .build());
    players.add(Player.builder()
        .email("bad-one@texastoc.com")
        .roles(ImmutableSet.of(Role.builder()
            .type(Role.Type.USER)
            .build()))
        .build());
    players.add(Player.builder()
        .roles(ImmutableSet.of(Role.builder()
            .type(Role.Type.ADMIN)
            .build()))
        .build());
    players.add(Player.builder()
        .email("another-good-one@texastoc.com")
        .roles(ImmutableSet.of(Role.builder()
            .type(Role.Type.ADMIN)
            .build()))
        .build());

    NotificationModule notificationModule = mock(NotificationModule.class);

    // Act
    GameSummary gameSummary = new GameSummary(game, season, quarterlySeasons, players, 0);
    ReflectionTestUtils.setField(gameSummary, "notificationModule", notificationModule);
    gameSummary.run();

    // Assert
    ArgumentCaptor<String> bodyArg = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<List<String>> emailsArg = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<String> subjectArg = ArgumentCaptor.forClass(String.class);
    Mockito.verify(notificationModule, times(1))
        .sendEmail(emailsArg.capture(), subjectArg.capture(), bodyArg.capture());

    // Check email subject
    String actualSubject = subjectArg.getValue();
    Assert.assertEquals("Summary 2020-12-29", actualSubject);

    // Check the body
    String expecteBody = getExpectedBody();
    String actualBody = bodyArg.getValue();
    Assert.assertEquals(expecteBody, actualBody);

    // Check the list of emails to which the summary is sent
    List<String> emails = emailsArg.getValue();
    Assertions.assertThat(emails)
        .containsExactlyInAnyOrder("good-one@texastoc.com", "another-good-one@texastoc.com");
  }

  private String getExpectedBody() throws IOException {
    StringBuilder sb = new StringBuilder();
    InputStream resource = new ClassPathResource(
        "expected-game-summary.html").getInputStream();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource))) {
      String line;
      int count = 0;
      while ((line = reader.readLine()) != null) {
        if (count++ > 0) {
          sb.append("\n");
        }
        sb.append(line);
      }
    }
    return sb.toString();
  }
}