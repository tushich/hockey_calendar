import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

public class hockey_calendar {

    public static void main(String[] args) {

      /*  try {
            DataBase.executeSQLUpdate("UPDATE matches SET Tournament = 'awdawdawd' WHERE  matchID = '5617v78585';", null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }*/
        // DataBase.createTableUsers();
        // DataBase.createTableMatches();


        TelegramBot.getInstance().startListen();

        // TODO 0. Сделать подписку на оповещения по командам из чата. Привязку хранить в списке пользователей.
        // TODO 1. Подключить сайт ФХР СПБ.
        String[] teamIDArray = new String[2];
        teamIDArray[0] = Resources.getResource("teamIdSpbhl_red_bears_main");
        teamIDArray[1] = Resources.getResource("teamIdSpbhl_red_bears_farm");

        // TODO 3. СРавнивать состав команд. Могут поменять команду а ID оставить старый т.е. в боте больше матчей чем на сайте
        // TODO 4. Сделать отправку ошибок в чат Максиму Т
        // CalenderGoogle calender = new CalenderGoogle();
        // TODO подумать как создавать календарь для разных команд

        for (String teamID : teamIDArray) {

            List<Match> matchTable = siteSPBHL.getMatchTable(String.format("http://spbhl.ru/Schedule?TeamID=%s", teamID));

            for (Match match_from_site : matchTable) {

                String summary = match_from_site.getTeams() + "\nСтадион:" + match_from_site.getStadium() + "\nТурнир:" + match_from_site.getTournament();

                try {
                    Match match_from_dataBase = DataBase.getMatch(match_from_site.getMatchID());
                    if (match_from_dataBase.isEmpty()) // Если пустой, то создадим овый матч
                    {
                        if(!DataBase.addMatch(match_from_site))
                            continue; // если не добавили, то погнали дальше
                        TelegramBot.getInstance().sendMsg(String.format("Добавлен новый матч:\n*%s*. Дата: %s\n%s", summary, match_from_site.getStringStartDateTime(), match_from_site.getLinkMatch()));
                    } else // Матч уже есть в базеДанных, значит надо найти различия. Обновить. и Сообщить об обновлении.
                    {
                        String diff = match_from_dataBase.compare(match_from_site);
                        if (!diff.isEmpty()) {
                            if(!DataBase.updateMatch(match_from_site))
                                continue; // не удалось обновить
                            TelegramBot.getInstance().sendMsg(String.format("*%s.*\n %s\n\n%s", summary, diff, match_from_site.getLinkMatch()));
                        }
                    }
                } catch (Exception e) {
                    System.out.format("\nНе удалось обработать матч %s\n Summary:%s\n Ошибка:%s", match_from_site.getLinkMatch(), summary, e.getMessage());
                }
            }
        }
    }
}
