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
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(String.format("INSERT INTO users(userID, team, FIO, telegramLogin) values('%s','%s','%s','%s')", userID, team, FIO, telegramLogin));
                    //"CREATE TABLE users(userId varchar(40), team varchar(40), FIO varchar(40), telegramLogin varchar(40), PRIMARY KEY(userId))");

            connection.close();
        } catch (SQLException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    static boolean delUser(String userID, String team){

        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(
                    "DELETE FROM users WHERE userID='" + userID + "' and team='" + team + "'");
            connection.close();
        } catch (SQLException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
