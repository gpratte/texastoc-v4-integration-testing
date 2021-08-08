package com.texastoc.config.job;

import com.google.common.collect.ImmutableList;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.model.Seating;
import com.texastoc.module.game.model.SeatsPerTable;
import com.texastoc.module.game.model.TableRequest;
import com.texastoc.module.game.service.GamePlayerService;
import com.texastoc.module.game.service.GameService;
import com.texastoc.module.game.service.SeatingService;
import com.texastoc.module.player.PlayerModule;
import com.texastoc.module.player.PlayerModuleFactory;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.season.model.Season;
import com.texastoc.module.season.service.SeasonService;
import com.texastoc.module.settings.SettingsModule;
import com.texastoc.module.settings.SettingsModuleFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * When running with an embedded H2 database populate the current season with games.
 */
@Slf4j
@Component
public class Populator {

  private final SeasonService seasonService;
  private final GameService gameService;
  private final GamePlayerService gamePlayerService;
  private final SeatingService seatingService;
  private final Random random = new Random(System.currentTimeMillis());
  private final JdbcTemplate jdbcTemplate;
  private PlayerModule playerModule;
  private SettingsModule settingsModule;

  @Value("${db.schema:false}")
  private boolean schema;
  @Value("${db.seed:false}")
  private boolean seed;
  @Value("${db.populate:false}")
  private boolean populate;

  public Populator(SeasonService seasonService, GameService gameService,
      GamePlayerService gamePlayerService, SeatingService seatingService,
      JdbcTemplate jdbcTemplate) {
    this.seasonService = seasonService;
    this.gameService = gameService;
    this.gamePlayerService = gamePlayerService;
    this.seatingService = seatingService;
    this.jdbcTemplate = jdbcTemplate;
  }

  public void initializeData() {
    try {
      if (schema) {
        InputStream resource = new ClassPathResource("create_toc_schema.sql").getInputStream();
        applySql(resource);
      }
      if (seed) {
        InputStream resource = new ClassPathResource("seed_toc.sql").getInputStream();
        applySql(resource);
      }
      if (populate) {
        createSeason();
      }
    } catch (IOException e) {
      log.error("Problem initializing data", e);
    }
  }

  private void createSeason() {
    LocalDate now = LocalDate.now();

    try {
      List<Season> seasons = seasonService.getAll();
      if (seasons.size() > 0) {
        return;
      }
      log.info("Populating");
      int year;
      switch (now.getMonth()) {
        case JANUARY:
        case FEBRUARY:
        case MARCH:
        case APRIL:
          // if before May then create for the previous year
          year = now.getYear() - 1;
          break;
        default:
          year = now.getYear();
      }
      Season season = seasonService.create(year);
      createGames(season);
      log.info("\nDone populating");
    } catch (Exception e) {
      log.error("Problem populating", e);
    }
  }

  private void createGames(Season season) {
    LocalDate now = LocalDate.now();

    LocalDate seasonStart = season.getStart();
    LocalDate gameDate = findNextThursday(seasonStart);

    while (!gameDate.isAfter(now)) {
      // pick one of the first players to be the host
      List<Player> players = getPlayerModule().getAll();
      int numPlayers = players.size();
      Player player = null;
      if (numPlayers > 5) {
        player = players.get(random.nextInt(5));
      } else {
        player = players.get(random.nextInt(numPlayers));
      }

      log.info(".");
      Game game = gameService.create(Game.builder()
          .hostId(player.getId())
          .date(gameDate)
          .transportRequired(false)
          .build(), season.getId());

      addGamePlayers(game.getId());
      seatGamePlayers(game.getId());
      addGamePlayersRebuy(game.getId());
      addGamePlayersFinish(game.getId());

      // Is this the last game? Check if the next game is after now.
      LocalDate nextGameDate = findNextThursday(gameDate.plusDays(1));
      if (!nextGameDate.isAfter(now)) {
        // finalize the game
        gameService.finalize(game.getId());
      }
      gameDate = findNextThursday(gameDate.plusDays(1));
    }
  }

  private void addGamePlayers(int gameId) {
    Game game = gameService.get(gameId);

    int numPlayersToAddToGame = game.getDate().getDayOfMonth();
    if (numPlayersToAddToGame < 2) {
      numPlayersToAddToGame = 2;
    }

    List<Player> existingPlayers = getPlayerModule().getAll();

    if (existingPlayers.size() < 30) {
      addNewPlayer(game);
      numPlayersToAddToGame -= 1;
    }

    if (existingPlayers.size() >= numPlayersToAddToGame) {
      // use the existing players
      List<Integer> existingPlayersIdsInGame = new ArrayList<>(existingPlayers.size());

      // Grab an existing player if not already added to game
      while (numPlayersToAddToGame > 0) {
        Player existingPlayer = existingPlayers.get(random.nextInt(existingPlayers.size()));
        if (existingPlayersIdsInGame.contains(existingPlayer.getId())) {
          continue;
        }
        // Add existing player to the game
        addExistingPlayer(game, existingPlayer);
        existingPlayersIdsInGame.add(existingPlayer.getId());
        --numPlayersToAddToGame;
      }
    } else {
      // not enough existing players so use all existing players and then add new players
      for (Player existingPlayer : existingPlayers) {
        addExistingPlayer(game, existingPlayer);
        --numPlayersToAddToGame;
      }

      // now add new players
      for (int i = 0; i < numPlayersToAddToGame; i++) {
        addNewPlayer(game);
      }
    }
  }

  private void seatGamePlayers(int gameId) {
    Game game = gameService.get(gameId);

    // one table for every 8 players
    int numPlayers = game.getNumPlayers();
    int numTables = numPlayers / 8;
    boolean remainder = (game.getNumPlayers() % 8) > 0;
    numTables = remainder ? ++numTables : numTables;

    List<SeatsPerTable> seatsPerTables = new LinkedList<>();
    for (int i = 1; i <= numTables; i++) {
      SeatsPerTable seatsPerTable = new SeatsPerTable();
      seatsPerTables.add(seatsPerTable);
      seatsPerTable.setNumSeats(8);
      seatsPerTable.setTableNum(i);
    }

    Seating seating = new Seating();
    seating.setSeatsPerTables(seatsPerTables);

    // One table request
    GamePlayer gamePlayer = game.getPlayers().stream()
        .findAny().get();
    TableRequest tableRequest = new TableRequest();
    tableRequest.setGamePlayerId(gamePlayer.getId());
    tableRequest.setGamePlayerName(gamePlayer.getName());
    tableRequest.setTableNum(1);
    seating.setTableRequests(ImmutableList.of(tableRequest));

    seating.setGameId(game.getId());
    seatingService.seatGamePlayers(seating);
  }

  private void addGamePlayersRebuy(int gameId) {
    Game game = gameService.get(gameId);

    List<GamePlayer> gamePlayers = game.getPlayers();
    for (GamePlayer gamePlayer : gamePlayers) {
      if (random.nextBoolean()) {
        gamePlayer.setRebought(true);
        gamePlayerService.updateGamePlayer(gamePlayer);
      }
    }
  }

  private void addGamePlayersFinish(int gameId) {
    Game game = gameService.get(gameId);

    // Make a copy of the list
    List<GamePlayer> gamePlayers = new ArrayList<>(game.getPlayers());

    for (int place = 1; place <= 10 && gamePlayers.size() > 0; ++place) {
      GamePlayer gamePlayer = gamePlayers.remove(random.nextInt(gamePlayers.size()));
      gamePlayer.setPlace(place);
      gamePlayerService.updateGamePlayer(gamePlayer);
    }
  }

  private void addExistingPlayer(Game game, Player existingPlayer) {
    GamePlayer gamePlayer = new GamePlayer();
    gamePlayer.setGameId(game.getId());
    gamePlayer.setPlayerId(existingPlayer.getId());
    gamePlayer.setBoughtIn(true);
    if (random.nextBoolean()) {
      gamePlayer.setAnnualTocParticipant(true);
    }
    // 20% in the quarterly
    if (random.nextInt(5) == 0) {
      gamePlayer.setQuarterlyTocParticipant(true);
    }
    gamePlayerService.createGamePlayer(gamePlayer);
  }

  private void addNewPlayer(Game game) {
    GamePlayer gamePlayer = new GamePlayer();
    gamePlayer.setGameId(game.getId());
    int firstNameIndex = random.nextInt(300);
    gamePlayer.setFirstName(firstNames[firstNameIndex]);
    int lastNameIndex = random.nextInt(300);
    gamePlayer.setLastName(lastNames[lastNameIndex]);
    gamePlayer.setBoughtIn(true);
    if (random.nextBoolean()) {
      gamePlayer.setAnnualTocParticipant(true);
    }
    if (random.nextBoolean()) {
      gamePlayer.setQuarterlyTocParticipant(true);
    }
    gamePlayerService.createFirstTimeGamePlayer(gamePlayer);
  }

  private LocalDate findNextThursday(LocalDate date) {
    while (date.getDayOfWeek() != DayOfWeek.THURSDAY) {
      date = date.plusDays(1);
    }
    return date;
  }


  private PlayerModule getPlayerModule() {
    if (playerModule == null) {
      playerModule = PlayerModuleFactory.getPlayerModule();
    }
    return playerModule;
  }

  private SettingsModule getSettingsModule() {
    if (settingsModule == null) {
      settingsModule = SettingsModuleFactory.getSettingsModule();
    }
    return settingsModule;
  }

  private void applySql(InputStream resource) throws IOException {
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(resource))) {
      String line;
      StringBuilder sb = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        if (StringUtils.isBlank(line)) {
          continue;
        }
        if (line.startsWith("#")) {
          continue;
        }

        sb.append(" ").append(line);

        if (line.endsWith(";")) {
          jdbcTemplate.execute(sb.toString());
          sb = new StringBuilder();
        }
      }
    }
  }


  static final String[] firstNames = {"James", "John", "Robert", "Michael", "Mary", "William",
      "David", "Joseph", "Richard", "Charles", "Thomas", "Christopher", "Daniel", "Elizabeth",
      "Matthew", "Patricia", "George", "Jennifer", "Linda", "Anthony", "Barbara", "Donald", "Paul",
      "Mark", "Andrew", "Edward", "Steven", "Kenneth", "Margaret", "Joshua", "Kevin", "Brian",
      "Susan", "Dorothy", "Ronald", "Sarah", "Timothy", "Jessica", "Jason", "Helen", "Nancy",
      "Betty", "Karen", "Jeffrey", "Lisa", "Ryan", "Jacob", "Frank", "Gary", "Nicholas", "Anna",
      "Eric", "Sandra", "Stephen", "Emily", "Ashley", "Jonathan", "Kimberly", "Donna", "Ruth",
      "Carol", "Michelle", "Larry", "Laura", "Amanda", "Justin", "Raymond", "Scott", "Samuel",
      "Brandon", "Melissa", "Benjamin", "Rebecca", "Deborah", "Stephanie", "Sharon", "Kathleen",
      "Cynthia", "Gregory", "Jack", "Amy", "Henry", "Shirley", "Patrick", "Alexander", "Emma",
      "Angela", "Catherine", "Virginia", "Katherine", "Walter", "Dennis", "Jerry", "Brenda",
      "Pamela", "Frances", "Tyler", "Nicole", "Christine", "Aaron", "Peter", "Samantha", "Evelyn",
      "Jose", "Rachel", "Alice", "Douglas", "Janet", "Carolyn", "Adam", "Debra", "Harold", "Nathan",
      "Martha", "Maria", "Marie", "Zachary", "Arthur", "Heather", "Diane", "Julie", "Joyce", "Carl",
      "Grace", "Victoria", "Albert", "Rose", "Joan", "Kyle", "Christina", "Kelly", "Ann", "Lauren",
      "Doris", "Julia", "Jean", "Lawrence", "Judith", "Olivia", "Kathryn", "Joe", "Mildred",
      "Willie", "Gerald", "Lillian", "Roger", "Cheryl", "Megan", "Jeremy", "Keith", "Hannah",
      "Andrea", "Ethan", "Sara", "Terry", "Jacqueline", "Christian", "Harry", "Jesse", "Sean",
      "Teresa", "Ralph", "Austin", "Gloria", "Janice", "Roy", "Theresa", "Louis", "Noah", "Bruce",
      "Billy", "Judy", "Bryan", "Madison", "Eugene", "Beverly", "Jordan", "Denise", "Jane",
      "Marilyn", "Amber", "Dylan", "Danielle", "Abigail", "Charlotte", "Diana", "Brittany",
      "Russell", "Natalie", "Wayne", "Irene", "Ruby", "Annie", "Sophia", "Alan", "Juan", "Gabriel",
      "Howard", "Fred", "Vincent", "Lori", "Philip", "Kayla", "Alexis", "Tiffany", "Florence",
      "Isabella", "Kathy", "Louise", "Logan", "Lois", "Tammy", "Crystal", "Randy", "Bonnie",
      "Phyllis", "Anne", "Taylor", "Victor", "Bobby", "Erin", "Johnny", "Phillip", "Martin",
      "Josephine", "Alyssa", "Bradley", "Ella", "Shawn", "Clarence", "Travis", "Ernest", "Stanley",
      "Allison", "Craig", "Shannon", "Elijah", "Edna", "Peggy", "Tina", "Leonard", "Robin", "Dawn",
      "Carlos", "Earl", "Eleanor", "Jimmy", "Francis", "Cody", "Caleb", "Mason", "Rita", "Danny",
      "Isaac", "Audrey", "Todd", "Wanda", "Clara", "Ethel", "Paula", "Cameron", "Norma", "Dale",
      "Ellen", "Luis", "Alex", "Marjorie", "Luke", "Jamie", "Nathaniel", "Allen", "Leslie", "Joel",
      "Evan", "Edith", "Connie", "Eva", "Gladys", "Carrie", "Ava", "Frederick", "Wendy", "Hazel",
      "Valerie", "Curtis", "Elaine", "Courtney", "Esther", "Cindy", "Vanessa", "Brianna", "Lucas",
      "Norman", "Marvin", "Tracy", "Tony", "Monica", "Antonio", "Glenn", "Melanie"};

  static final String[] lastNames = {"Smith", "Johnson", "Williams", "Jones", "Brown", "Davis",
      "Miller", "Wilson", "Moore", "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris",
      "Martin", "Thompson", "GArcia", "Martinez", "Robinson", "Clark", "Rodriguez", "Lewis", "Lee",
      "Walker", "Hall", "Allen", "Young", "Hernandez", "King", "Wright", "Lopez", "Hill", "Scott",
      "Green", "Adams", "Baker", "Gonzalez", "Nelson", "Carter", "Mitchell", "Perez", "Roberts",
      "Turner", "Phillips", "Campbell", "Parker", "Evans", "Edwards", "Collins", "Stewart",
      "Sanchez", "Morris", "Rogers", "Reed", "Cook", "Morgan", "Bell", "Murphy", "Bailey", "Rivera",
      "Cooper", "Richardson", "Cox", "Howard", "Ward", "Torres", "Peterson", "Gray", "Ramirez",
      "James", "Watson", "Brooks", "Kelly", "Sanders", "Price", "Bennett", "Wood", "Barnes", "Ross",
      "Henderson", "Coleman", "Jenkins", "Perry", "Powell", "Long", "Patterson", "Hughes", "Flores",
      "Washington", "Butler", "Simmons", "Foster", "Gonzales", "Bryant", "Alexander", "Russell",
      "Griffin", "Diaz", "Hayes", "Myers", "Ford", "Hamilton", "Graham", "Sullivan", "Wallace",
      "Woods", "Cole", "West", "Jordan", "Owens", "Reynolds", "Fisher", "Ellis", "Harrison",
      "Gibson", "McDonald", "Cruz", "Marshall", "Ortiz", "Gomez", "Murray", "Freeman", "Wells",
      "Webb", "Simpson", "Stevens", "Tucker", "Porter", "Hunter", "Hicks", "Crawford", "Henry",
      "Boyd", "Mason", "Morales", "Kennedy", "Warren", "Dixon", "Ramos", "Reyes", "Burns", "Gordon",
      "Shaw", "Holmes", "Rice", "Robertson", "Hunt", "Black", "Daniels", "Palmer", "Mills",
      "Nichols", "Grant", "Knight", "Ferguson", "Rose", "Stone", "Hawkins", "Dunn", "Perkins",
      "Hudson", "Spencer", "Gardner", "Stephens", "Payne", "Pierce", "Berry", "Matthews", "Arnold",
      "Wagner", "Willis", "Ray", "Watkins", "Olson", "Carroll", "Duncan", "Snyder", "Hart",
      "Cunningham", "Bradley", "Lane", "Andrews", "Ruiz", "Harper", "Fox", "Riley", "Armstrong",
      "Carpenter", "Weaver", "Greene", "Lawrence", "Elliott", "Chavez", "Sims", "Austin", "Peters",
      "Kelley", "Franklin", "Lawson", "Fields", "Gutierrez", "Ryan", "Schmidt", "Carr", "Vasquez",
      "Castillo", "Wheeler", "Chapman", "Oliver", "Montgomery", "Richards", "Williamson",
      "Johnston", "Banks", "Meyer", "Bishop", "McCoy", "Howell", "Alvarez", "Morrison", "Hansen",
      "Fernandez", "Garza", "Harvey", "Little", "Burton", "Stanley", "Nguyen", "George", "Jacobs",
      "Reid", "Kim", "Fuller", "Lynch", "Dean", "Gilbert", "Garrett", "Romero", "Welch", "Larson",
      "Frazier", "Burke", "Hanson", "Day", "Mendoza", "Moreno", "Bowman", "Medina", "Fowler",
      "Brewer", "Hoffman", "Carlson", "Silva", "Pearson", "Holland", "Douglas", "Fleming", "Jensen",
      "Vargas", "Byrd", "Davidson", "Hopkins", "May", "Terry", "Herrera", "Wade", "Soto", "Walters",
      "Curtis", "Neal", "Caldwell", "Lowe", "Jennings", "Barnett", "Graves", "Jimenez", "Horton",
      "Shelton", "Barrett", "Obrien", "Castro", "Sutton", "Gregory", "McKinney", "Lucas", "Miles",
      "Craig", "Rodriquez", "Chambers", "Holt", "Lambert", "Fletcher", "Watts", "Bates", "Hale",
      "Rhodes", "Pena", "Beck", "Newman"
  };
}
