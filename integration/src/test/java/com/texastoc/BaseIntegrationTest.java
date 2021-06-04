package com.texastoc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.model.Seating;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import com.texastoc.module.quarterly.model.QuarterlySeason;
import com.texastoc.module.season.model.Season;
import com.texastoc.module.settings.model.SystemSettings;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public abstract class BaseIntegrationTest implements TestConstants {

  private final String SERVER_URL = "http://localhost";
  private String V4_ENDPOINT;

  private final int port = 8080;

  protected RestTemplate restTemplate;

  public BaseIntegrationTest() {
    HttpClient client = HttpClients.createDefault();
    restTemplate = new RestTemplate();
    restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(client));
  }

  protected String endpoint() {
    if (V4_ENDPOINT == null) {
      V4_ENDPOINT = SERVER_URL + ":" + port + "/api/v4";
    }
    return V4_ENDPOINT;
  }

  protected String endpointRoot() {
    return SERVER_URL + ":" + port;
  }

  protected LocalDate getSeasonStart() {
    LocalDate now = LocalDate.now();
    LocalDate start = null;
    if (now.getMonthValue() < 5) {
      start = LocalDate.of(now.getYear() - 1, Month.MAY, 1);
    } else {
      start = LocalDate.of(now.getYear(), Month.MAY, 1);
    }
    return start;
  }

  protected Season createSeason(String token) throws Exception {
    return createSeason(getSeasonStart().getYear(), token);
  }

  protected Season createSeason(int startYear, String token) throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + token);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());

    SeasonStart seasonStart = new SeasonStart();
    seasonStart.setStartYear(startYear);
    String seasonAsJson = mapper.writeValueAsString(seasonStart);
    HttpEntity<String> entity = new HttpEntity<>(seasonAsJson, headers);

    return restTemplate.postForObject(endpoint() + "/seasons", entity, Season.class);
  }

  protected Game createGame(Game gameToCreate, int seasonid, String token)
      throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + token);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());

    String gameToCreateAsJson = mapper.writeValueAsString(gameToCreate);
    HttpEntity<String> entity = new HttpEntity<>(gameToCreateAsJson, headers);

    return restTemplate
        .postForObject(endpoint() + "/seasons/" + seasonid + "/games", entity, Game.class);
  }

  protected void updateGame(int gameId, Game gameToUpdate, String token)
      throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + token);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String updateGameRequestAsJson = mapper.writeValueAsString(gameToUpdate);
    HttpEntity<String> entity = new HttpEntity<>(updateGameRequestAsJson, headers);

    ResponseEntity<Void> response = restTemplate.exchange(
        endpoint() + "/games/" + gameId,
        HttpMethod.PATCH,
        entity,
        Void.class);
  }

  protected GamePlayer addPlayerToGame(GamePlayer gamePlayer, String token)
      throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + token);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String createGamePlayerRequestAsJson = mapper.writeValueAsString(gamePlayer);
    HttpEntity<String> entity = new HttpEntity<>(createGamePlayerRequestAsJson, headers);

    return restTemplate
        .postForObject(endpoint() + "/games/" + gamePlayer.getGameId() + "/players", entity,
            GamePlayer.class);
  }

  protected GamePlayer addFirstTimePlayerToGame(GamePlayer gamePlayer, String token)
      throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/vnd.texastoc.first-time+json");
    headers.set("Authorization", "Bearer " + token);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String firstTimeGamePlayerRequestAsJson = mapper.writeValueAsString(gamePlayer);
    HttpEntity<String> entity = new HttpEntity<>(firstTimeGamePlayerRequestAsJson, headers);

    return restTemplate
        .postForObject(endpoint() + "/games/" + gamePlayer.getGameId() + "/players", entity,
            GamePlayer.class);
  }

  protected void updatePlayerInGame(GamePlayer gamePlayer, String token)
      throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + token);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String gamePlayerAsJson = mapper.writeValueAsString(gamePlayer);
    HttpEntity<String> entity = new HttpEntity<>(gamePlayerAsJson, headers);

    ResponseEntity<Void> response = restTemplate.exchange(
        endpoint() + "/games/" + gamePlayer.getGameId() + "/players/" + gamePlayer.getId(),
        HttpMethod.PATCH,
        entity,
        Void.class);
  }

  protected void deletePlayerFromGame(int gameId, int gamePlayerId, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    HttpEntity<String> entity = new HttpEntity<>("", headers);

    ResponseEntity<Void> response = restTemplate.exchange(
        endpoint() + "/games/" + gameId + "/players/" + gamePlayerId,
        HttpMethod.DELETE,
        entity,
        Void.class);
  }

  protected void finalizeGame(int gameId, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    headers.set("Content-Type", "application/vnd.texastoc.finalize+json");

    HttpEntity<String> entity = new HttpEntity<>(headers);
    restTemplate.put(endpoint() + "/games/" + gameId, entity);
  }

  protected void unfinalizeGame(int gameId, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    headers.set("Content-Type", "application/vnd.texastoc.unfinalize+json");

    HttpEntity<String> entity = new HttpEntity<>(headers);
    restTemplate.put(endpoint() + "/games/" + gameId, entity);
  }

  protected Seating seatPlayers(int gameId, Seating seating, String token) throws Exception {

    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/vnd.texastoc.assign-seats+json");
    headers.set("Authorization", "Bearer " + token);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String seatingRequestAsJson = mapper.writeValueAsString(seating);
    HttpEntity<String> entity = new HttpEntity<>(seatingRequestAsJson, headers);

    return restTemplate
        .postForObject(endpoint() + "/games/" + gameId + "/seats", entity, Seating.class);
  }

  protected Player createPlayer(Player player, String token) throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + token);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String playerRequestAsJson = mapper.writeValueAsString(player);
    HttpEntity<String> entity = new HttpEntity<>(playerRequestAsJson, headers);

    return restTemplate.postForObject(endpoint() + "/players", entity, Player.class);
  }

  protected void updatePlayer(Player player, String token) throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + token);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String playerRequestAsJson = mapper.writeValueAsString(player);
    HttpEntity<String> entity = new HttpEntity<>(playerRequestAsJson, headers);

    restTemplate.put(endpoint() + "/players/" + player.getId(), entity);
  }

  protected void deletePlayer(int playerId, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    HttpEntity<String> entity = new HttpEntity<>("", headers);

    restTemplate.exchange(endpoint() + "/players/" + playerId,
        HttpMethod.DELETE,
        entity,
        Void.class);
  }

  protected void addRole(int playerId, Role role, String token) throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + token);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String addRoleRequestAsJson = mapper.writeValueAsString(role);
    HttpEntity<String> entity = new HttpEntity<>(addRoleRequestAsJson, headers);

    restTemplate.exchange(endpoint() + "/players/" + playerId + "/roles",
        HttpMethod.POST,
        entity,
        Void.class);
  }

  protected void removeRole(int playerId, int roleId, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    HttpEntity<String> entity = new HttpEntity<>("", headers);

    restTemplate.exchange(endpoint() + "/players/" + playerId + "/roles/" + roleId,
        HttpMethod.DELETE,
        entity,
        Void.class);
  }


  protected Player getPlayer(int id, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<Player> response = restTemplate.exchange(
        endpoint() + "/players/" + id,
        HttpMethod.GET,
        entity,
        Player.class);
    return response.getBody();
  }

  protected List<Player> getPlayers(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<List<Player>> response = restTemplate.exchange(
        endpoint() + "/players",
        HttpMethod.GET,
        entity,
        new ParameterizedTypeReference<List<Player>>() {
        });
    return response.getBody();
  }

  protected SystemSettings getSettings() {
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<SystemSettings> response = restTemplate.exchange(
        endpoint() + "/settings",
        HttpMethod.GET,
        entity,
        SystemSettings.class);
    return response.getBody();
  }

  protected Game getGame(int id, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    HttpEntity<String> entity = new HttpEntity<>("", headers);

    ResponseEntity<Game> response = restTemplate.exchange(
        endpoint() + "/games/" + id,
        HttpMethod.GET,
        entity,
        Game.class);
    return response.getBody();
  }

  protected Game getGameBySeasonId(int seasonId, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    HttpEntity<String> entity = new HttpEntity<>("", headers);

    ResponseEntity<List<Game>> response = restTemplate.exchange(
        endpoint() + "/seasons/" + seasonId + "/games",
        HttpMethod.GET,
        entity,
        new ParameterizedTypeReference<List<Game>>() {
        });
    return response.getBody().get(0);
  }

  protected Season getSeason(int id, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    HttpEntity<String> entity = new HttpEntity<>("", headers);

    ResponseEntity<Season> response = restTemplate.exchange(
        endpoint() + "/seasons/" + id,
        HttpMethod.GET,
        entity,
        Season.class);
    return response.getBody();
  }

  protected void endSeason(int seasonId, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    headers.set("Content-Type", "application/vnd.texastoc.finalize+json");

    HttpEntity<String> entity = new HttpEntity<>(headers);
    restTemplate.put(endpoint() + "/seasons/" + seasonId, entity);
  }

  protected void openSeason(int seasonId, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    headers.set("Content-Type", "application/vnd.texastoc.unfinalize+json");

    HttpEntity<String> entity = new HttpEntity<>(headers);
    restTemplate.put(endpoint() + "/seasons/" + seasonId, entity);
  }

  protected List<QuarterlySeason> getQuarterlySeasons(int seasonId, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<List<QuarterlySeason>> response = restTemplate.exchange(
        endpoint() + "/seasons/" + seasonId + "/quarterlies",
        HttpMethod.GET,
        entity,
        new ParameterizedTypeReference<List<QuarterlySeason>>() {
        });
    return response.getBody();
  }

  protected String login(String email, String password) throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());

    LoginParameters loginParameters = new LoginParameters();
    loginParameters.email = email;
    loginParameters.password = password;
    String loginParametersAsJson = mapper.writeValueAsString(loginParameters);
    HttpEntity<String> entity = new HttpEntity<>(loginParametersAsJson, headers);

    String url = endpointRoot() + "/login";
    return restTemplate.postForObject(url, entity, Token.class).getToken();
  }


  @Getter
  @Setter
  private static class LoginParameters {

    String email;
    String password;
  }

  @Getter
  @Setter
  private static class Token {

    String token;
  }

  @Getter
  @Setter
  public static class SeasonStart {

    private int startYear;
  }

}
