import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// TODO 4. Если не получилось выполнить SQL операцию сделать отправку ошибок в чат Максиму Т.

public interface DataBase {

    static boolean addUser(String userID, String team, String FIO, String telegramLogin) {
        try {
            return executeSQLUpdate(String.format("INSERT INTO users(userID, team, FIO, telegramLogin) values('%s','%s','%s','%s')", userID, team, FIO, telegramLogin), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static boolean delUser(String userID, String team) {
        try {
            return executeSQLUpdate("DELETE FROM users WHERE userID='" + userID + "' and team='" + team + "'", null);
        } catch (SQLException | URISyntaxException e) {
            System.out.format("\nОшибка удаления пользователя:%s\n Команда:%s\nТекст ошибки:%s", userID, team, e.getMessage());
            throw new RuntimeException(e);
        }

    }

    static List<String> getUsersList(String team) {
        List<String> list = new ArrayList<>();
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();

            //Выполним запрос
            ResultSet result1 = statement.executeQuery(
                    "SELECT userID FROM users where team='" + team + "'");
            while (result1.next()) {
                list.add(result1.getString("userId"));
            }
            connection.close();
        } catch (SQLException | URISyntaxException e) {
            e.printStackTrace();
        }
        return list;
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
                match.setMatchID(result1.getString("site_id"));
            }
            connection.close();
        } catch (SQLException | URISyntaxException e) {
            System.out.format("\nОшибка поиска матча %s\nТекст ошибки:%s", matchId, e.getMessage());
            throw new RuntimeException(e);
        }
        return match;
    }

    static boolean addMatch(Match match) {
        try {
            return executeSQLUpdate(
                    String.format("INSERT INTO " +
                                    "matches(matchID, Tournament, Round, Number, startDateTime, Stadium, teams, count, protokolExist, linkMatch, team_id, site_id) " +
                                    "values('%s','%s','%s','%s',?,'%s','%s','%s','%s','%s', '%s')",
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
            System.out.format("\nОшибка добавления матча %s\nТекст ошибки:%s", match.getMatchID(), e.getMessage());
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
                                    "team_id = '%s' " +
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
            System.out.format("\nОшибка обновления матча %s\nТекст ошибки:%s", match.getMatchID(), e.getMessage());
            throw new RuntimeException(e);
        }

    }

    static boolean addubscription(String userID, String team_name, String team_id, String site_id) {
        try {
            return executeSQLUpdate(String.format("INSERT INTO subscriptions(userId, team_name, team_id, site_id) values('%s','%s','%s','%s')", userID, team_name, team_id, site_id), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static boolean delSubscription(String userID, String team_id, String site_id) {
        try {
            return executeSQLUpdate("DELETE FROM subscriptions WHERE userID='" + userID + "' and team_id='" + team_id + "'" + "' and site_id='" + site_id + "'", null);
        } catch (SQLException | URISyntaxException e) {
            System.out.format("\nОшибка удаления подписки User:%s\nteam_id:%s\nsite_id:%s\nТекст ошибки:%s", userID, team_id, site_id, e.getMessage());
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
                    "SELECT userid FROM subcriptions where team_id='" + team_id + "'" + " and site_id=" + site_id);
            while (result1.next()) {
                list.add(result1.getString("userid"));
            }
            connection.close();
        } catch (SQLException | URISyntaxException e) {
            e.printStackTrace();
        }
        return list;
    }

    @SuppressWarnings("unused")
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
                    "linkMatch varchar(100),  " +
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
            return executeSQLUpdate("CREATE TABLE subscriptions(userId varchar(40), team_name varchar(40), team_id varchar(40), site_id varchar(40))", null);
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
        URI dbUri = new URI(Resources.getResource("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath() + "?sslmode=require";

        return DriverManager.getConnection(dbUrl, username, password);
    }
}
