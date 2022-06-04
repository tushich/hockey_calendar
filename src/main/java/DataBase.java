import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public interface DataBase {
    static Connection getConnection() throws URISyntaxException, SQLException {
        URI dbUri = new URI(Resources.getResource("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath() + "?sslmode=require";

        return DriverManager.getConnection(dbUrl, username, password);
    }

    static List<String> getUsersList(String team)  {
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

    static boolean addUser(String userID, String team, String FIO, String telegramLogin){
        return executeSQLUpdate(String.format("INSERT INTO users(userID, team, FIO, telegramLogin) values('%s','%s','%s','%s')", userID, team, FIO, telegramLogin));
    }

    static Match getMatch(String matchId){

        Match match = new Match();

        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();

            //Выполним запрос
            ResultSet result1 = statement.executeQuery(
                    "SELECT * FROM matches where matchId='" + matchId + "'");
           if(result1.next()) {
               for (int i = 0; i < match.colNames.length; i++)
               {
                   if(i == 3 || i == 4) // дату собираем из 2х строк
                   {
                       match.setStartDateTime(result1.getString(match.colNames[3]), result1.getString(match.colNames[4]));
                       i = 4;
                       continue;
                   }
                   else
                       match.setByColumnId(i, result1.getString(match.colNames[i]));
               }
            }
            connection.close();
        } catch (SQLException | URISyntaxException e) {
            e.printStackTrace();
        }
        return match;
    }

    static boolean addMatch(Match match){
        return executeSQLUpdate(
                String.format("INSERT INTO " +
                        "matches(matchID, Tournament, Round, Number, startDateTime, Stadium, teams, count, protokolExist, linkMatch) " +
                        "values('%s','%s','%s','%s','%t','%s','%s','%s','%s','%s')",
                        match.getMatchID(),
                        match.getTournament(),
                        match.getRound(),
                        match.getNumber(),
                        new Timestamp(match.getStartDateTime().getValue()),
                        match.getStadium(),
                        match.getTeams(),
                        match.getCount(),
                        match.getProtokolExist(),
                        match.getLinkMatch()));
        // TODO как добавлять дату в запросы sql
    }
    static boolean updateMatch(Match match){
        return executeSQLUpdate(
                String.format("UPDATE matches SET " +
                                "Tournament = '%s', " +
                                "Round = '%s', " +
                                "Number = '%s', " +
                                "startDateTime = '%t', " +
                                "Stadium = '%s', " +
                                "teams = '%s', " +
                                "count = '%s', " +
                                "protokolExist = '%s', " +
                                "linkMatch = '%s' " +
                                "WHERE  matchID = '%s'"
                               ,match.getTournament(),
                                match.getRound(),
                                match.getNumber(),
                                new Timestamp(match.getStartDateTime().getValue()),
                                match.getStadium(),
                                match.getTeams(),
                                match.getCount(),
                                match.getProtokolExist(),
                                match.getLinkMatch(),
                                match.getMatchID()));
        // TODO как добавлять дату в запросы sql
    }

    static boolean delUser(String userID, String team){
        return executeSQLUpdate("DELETE FROM users WHERE userID='" + userID + "' and team='" + team + "'");
    }

    static boolean createTableMatches(){
        return executeSQLUpdate(" CREATE TABLE " +
                "matches(matchID varchar(40), " +
                "Tournament varchar(40), " +
                "Round varchar(40), " +
                "Number varchar(40), " +
                "startDateTime timestamp with time zone, " +
                "Stadium varchar(40), " +
                "teams varchar(40), " +
                "count varchar(40), " +
                "protokolExist BOOLEAN NOT NULL DEFAULT false, " +
                "linkMatch varchar(100),  " +
                "PRIMARY KEY(matchID))");
    }
    static boolean createTableUsers(){
        return executeSQLUpdate("CREATE TABLE users(userId varchar(40), team varchar(40), FIO varchar(40), telegramLogin varchar(40), PRIMARY KEY(userId))");
    }

    static boolean executeSQLUpdate(String request){
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(request);
            connection.close();
        } catch (SQLException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
