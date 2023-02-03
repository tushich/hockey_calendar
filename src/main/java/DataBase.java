import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.*;
import java.util.Date;

public interface DataBase {

    String DATABASE_URL = Resources.getResource("DATABASE_URL2");

    static boolean addUser(String userID, String FIO, String telegramLogin) {
        try {
            return executeSQLUpdate(String.format("INSERT INTO users(userID, FIO, telegramLogin) values('%s','%s','%s')", userID, FIO, telegramLogin), null);
        } catch (Exception e) {
            String errText = String.format("\nОшибка добавления пользователя.\n ФИО: %s\n Логин: %s\nТекст ошибки: %s", FIO, telegramLogin, e.getMessage());
            System.out.format(errText);
            TelegramBot.getInstance().sendMsgToAdmin(errText);
            throw new RuntimeException(e);
        }
    }

    static boolean delUser(String userID) {
        try {
            executeSQLUpdate(String.format("DELETE FROM subscriptions WHERE userID='%s'", userID), null);
            return executeSQLUpdate(String.format("DELETE FROM users WHERE userID='%s'", userID), null);
            // TODO Сделать удаление подписок и пользователя как единую транзакцию
        } catch (SQLException | URISyntaxException e) {
            String errText = String.format("\nОшибка удаления пользователя: %s\nТекст ошибки: %s", userID, e.getMessage());
            System.out.format(errText);
            TelegramBot.getInstance().sendMsgToAdmin(errText);
            throw new RuntimeException(e);
        }

    }

    static Map<String,Team> getUserSubscription(String userId) {
        Map<String,Team> teams = new HashMap<>();
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();

            //Выполним запрос
            ResultSet result1 = statement.executeQuery(
                    String.format("SELECT team_id, team_name, site_id FROM subscriptions where userId='%s'", userId));
            while (result1.next()) {
                teams.put(result1.getString("team_name"), new Team(result1.getString("team_name"), result1.getString("site_id"), result1.getString("team_id")));
            }
            connection.close();
        } catch (SQLException | URISyntaxException e) {
            String errText = String.format("\nОшибка получения списка подписок userId:%s\nТекст ошибки:%s", userId, e.getMessage());
            System.out.format(errText);
            TelegramBot.getInstance().sendMsgToAdmin(errText);
        }
        return teams;
    }

    static Match getMatch(String matchId) {

        Match match = new Match();

        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();

            //Выполним запрос
            ResultSet result1 = statement.executeQuery(
                    "SELECT * FROM matches where matchId='" + matchId + "'");
            if (result1.next()) {
                match.setTournament(result1.getString("Tournament"));
                match.setRound(result1.getString("Round"));
                match.setNumber(result1.getString("Number"));
                match.setStadium(result1.getString("Stadium"));
                match.setProtokolExist(result1.getBoolean("protokolExist"));
                match.setTeams(result1.getString("Teams"));
                match.setCount(result1.getString("Count"));
                match.setLinkMatch(result1.getString("LinkMatch"));
                match.setMatchID(result1.getString("MatchID"));
                match.setStartDateTime(new Date(result1.getTimestamp("StartDateTime").getTime()));
                match.setTeam_id(result1.getString("team_id"));
                match.setSiteID(result1.getString("site_id"));
            }
            connection.close();
        } catch (SQLException | URISyntaxException e) {
            String errText = String.format("\nОшибка поиска матча %s\nТекст ошибки:%s", matchId, e.getMessage());
            System.out.format(errText);
            TelegramBot.getInstance().sendMsgToAdmin(errText);
            throw new RuntimeException(e);
        }
        return match;
    }

    static boolean addMatch(Match match) {
        try {
            return executeSQLUpdate(
                    String.format("INSERT INTO " +
                                    "matches(matchID, Tournament, Round, Number, startDateTime, Stadium, teams, count, protokolExist, linkMatch, team_id, site_id) " +
                                    "values('%s','%s','%s','%s',?,'%s','%s','%s','%s','%s','%s','%s')",
                            match.getMatchID(),
                            match.getTournament(),
                            match.getRound(),
                            match.getNumber(),
                            match.getStadium(),
                            match.getTeams(),
                            match.getCount(),
                            match.getProtokolExist(),
                            match.getLinkMatch(),
                            match.getTeam_id(),
                            match.getSiteID()), new Timestamp(match.getStartDateTime().getTime()));
        } catch (SQLException | URISyntaxException e) {
            String errText = String.format("\nОшибка добавления матча %s\nТекст ошибки:%s", match.getMatchID(), e.getMessage());
            System.out.format(errText);
            TelegramBot.getInstance().sendMsgToAdmin(errText);
            throw new RuntimeException(e);
        }

    }

    static boolean updateMatch(Match match) {
        try {
            return executeSQLUpdate(
                    String.format("UPDATE matches SET " +
                                    "Tournament = '%s', " +
                                    "Round = '%s', " +
                                    "Number = '%s', " +
                                    "startDateTime = ?, " +
                                    "Stadium = '%s', " +
                                    "teams = '%s', " +
                                    "count = '%s', " +
                                    "protokolExist = '%s', " +
                                    "linkMatch = '%s', " +
                                    "team_id = '%s', " +
                                    "site_id = '%s' " +
                                    "WHERE  matchID = '%s'"
                            , match.getTournament(),
                            match.getRound(),
                            match.getNumber(),
                            match.getStadium(),
                            match.getTeams(),
                            match.getCount(),
                            match.getProtokolExist(),
                            match.getLinkMatch(),
                            match.getTeam_id(),
                            match.getSiteID(),
                            match.getMatchID()), new Timestamp(match.getStartDateTime().getTime()));
        } catch (SQLException | URISyntaxException e) {
            String errText = String.format("\nОшибка обновления матча %s\nТекст ошибки:%s", match.getMatchID(), e.getMessage());
            System.out.format(errText);
            TelegramBot.getInstance().sendMsgToAdmin(errText);
            throw new RuntimeException(e);
        }

    }

    static boolean addSubscription(String userID, String teamName, String team_id, String site_id) {
        try {
            Map<String, Team> subscriptions = getUserSubscription(userID);
            if(subscriptions.get(teamName) == null)
                return executeSQLUpdate(String.format("INSERT INTO subscriptions(userid, team_name, team_id, site_id) values('%s','%s','%s','%s')", userID, teamName, team_id, site_id), null);
            else
                return true;
        } catch (Exception e) {
            String errText = String.format("\nОшибка добавления подписки User:%s\nteam_id:%s\nsite_id:%s\nТекст ошибки:%s", userID, team_id, site_id, e.getMessage());
            System.out.format(errText);
            TelegramBot.getInstance().sendMsgToAdmin(errText);
            throw new RuntimeException(e);
        }
    }

    static boolean delSubscription(String userID, String team_id, String site_id) {
        try {
            return executeSQLUpdate(String.format("DELETE FROM subscriptions WHERE userID='%s' and team_id='%s' and site_id='%s'", userID, team_id, site_id), null);
        } catch (SQLException | URISyntaxException e) {
            String errText = String.format("\nОшибка удаления подписки User:%s\nteam_id:%s\nsite_id:%s\nТекст ошибки:%s", userID, team_id, site_id, e.getMessage());
            System.out.format(errText);
            TelegramBot.getInstance().sendMsgToAdmin(errText);
            throw new RuntimeException(e);
        }

    }

    static List<String> getUsersListSubscribedForTeam(String team_id, String site_id) {
        List<String> list = new ArrayList<>();
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();

            //Выполним запрос
            ResultSet result1 = statement.executeQuery(
                    String.format("SELECT userid FROM subscriptions where team_id='%s' and site_id='%s'", team_id, site_id));
            while (result1.next()) {
                list.add(result1.getString("userid"));
            }
            connection.close();
        } catch (SQLException | URISyntaxException e) {
            e.printStackTrace();
        }
        return list;
    }

    static boolean createTableMatches() {
        try {
            return executeSQLUpdate("CREATE TABLE " +
                    "matches(matchID varchar(20), " +
                    "Tournament varchar(100), " +
                    "Round varchar(100), " +
                    "Number varchar(10), " +
                    "startDateTime timestamp with time zone, " +
                    "Stadium varchar(40), " +
                    "teams varchar(100), " +
                    "count varchar(40), " +
                    "protokolExist BOOLEAN NOT NULL DEFAULT false, " +
                    "linkMatch varchar(100)," +
                    "Team_id varchar(40)," +
                    "Site_id varchar(40),  " +
                    "PRIMARY KEY(matchID))", null);
        } catch (SQLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    static boolean createTableUsers() {
        try {
            return executeSQLUpdate("CREATE TABLE users(userId varchar(40), team varchar(40), FIO varchar(40), telegramLogin varchar(40), PRIMARY KEY(userId))", null);
        } catch (SQLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    static boolean createTableSubscriptions() {
        try {
            return executeSQLUpdate("CREATE TABLE Subscriptions(userId varchar(40), team_name varchar(40), team_id varchar(40), site_id varchar(40))", null);
        } catch (SQLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean executeSQLUpdate(String request, Timestamp startDateTime) throws SQLException, URISyntaxException {

        Connection connection = getConnection();
        PreparedStatement prepareStatement = connection.prepareStatement(request);
        if (startDateTime != null)
            prepareStatement.setTimestamp(1, startDateTime);
        prepareStatement.executeUpdate();
        connection.close();
        return true;
    }

    static Connection getConnection() throws URISyntaxException, SQLException {
        URI dbUri = new URI(DATABASE_URL);
        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath() + "?sslmode=require";
        return DriverManager.getConnection(dbUrl, username, password);
    }

    static Map<String,Team> getTeams(){
        // TODO Сделать поиск команд из базы или с сайта
        Map<String,Team> teams = new HashMap<>();
        teams.put("Красные медведи ВЗР",        new Team("Красные медведи ВЗР",       "spbhl.ru", Resources.getResource("teamIdSpbhl_red_bears_main")));
        teams.put("Красные медведи ВЗР Фарм",   new Team("Красные медведи ВЗР Фарм",  "spbhl.ru", Resources.getResource("teamIdSpbhl_red_bears_farm")));
        teams.put("Красные медведи 2009",       new Team("Красные медведи 2009",      "fhspb.ru", Resources.getResource("team_red_bears_2009")));
        teams.put("Красные медведи 2011",       new Team("Красные медведи 2011",      "fhspb.ru", Resources.getResource("team_red_bears_2011")));
        teams.put("Красные медведи 2012",       new Team("Красные медведи 2012",      "fhspb.ru", Resources.getResource("team_red_bears_2012")));
        return teams;
    }
}
