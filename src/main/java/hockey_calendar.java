import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

public class hockey_calendar {

    public static void main(String[] args) {
        // TODO Сделать первоначальное заполнение настроек и таблиц. Например для переноса.
        // Процедуры первоначального заполнения. Использовались разово.
        /*
        try {
            DataBase.executeSQLUpdate("UPDATE matches SET Count = '0' WHERE  matchID = '5775v86469'", null);
        } catch (SQLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        try {
            DataBase.executeSQLUpdate("ALTER TABLE matches ADD team_id VARCHAR(40)", null);
        } catch (SQLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
         DataBase.createTableUsers();
         DataBase.createTableMatches();
        */

        TelegramBot.getInstance().startListen();

        // TODO 0. Подключить сайт ФХР СПБ.
        // TODO 1. Сделать подписку на оповещения по командам из чата. Привязку хранить в списке пользователей.

        // TODO 0 Получать список команд и списка подписок
        String[] teamIDArray = new String[2];
        teamIDArray[0] = Resources.getResource("teamIdSpbhl_red_bears_main");
        teamIDArray[1] = Resources.getResource("teamIdSpbhl_red_bears_farm");

        // TODO 3. СРавнивать состав команд. Могут поменять команду а ID оставить старый т.е. в боте больше матчей чем на сайте

        // CalenderGoogle calender = new CalenderGoogle();
        // TODO подумать как создавать календарь для разных команд

        for (String teamID : teamIDArray) {

            List<Match> matchTable = siteSPBHL.getMatchTable(teamID);

            for (Match match_from_site : matchTable) {

                String summary = match_from_site.getTeams() + "\nСтадион:" + match_from_site.getStadium() + "\nТурнир:" + match_from_site.getTournament();

                try {
                    Match match_from_dataBase = DataBase.getMatch(match_from_site.getMatchID());
                    if (match_from_dataBase.isEmpty()) // Если пустой, то создадим новый матч
                    {
                        if(!DataBase.addMatch(match_from_site))
                            continue; // если не добавили, то погнали дальше
                        TelegramBot.getInstance().sendBroadcastMsg(String.format("Добавлен новый матч:\n*%s*. Дата: %s\n%s", summary, match_from_site.getStringStartDateTime(), match_from_site.getLinkMatch()), match_from_site.getTeam_id());
                    } else // Матч уже есть в базеДанных, значит надо найти различия. Обновить. и Сообщить об обновлении.
                    {
                        String diff = match_from_dataBase.compare(match_from_site);
                        if (!diff.isEmpty()) {
                            if(!DataBase.updateMatch(match_from_site))
                                continue; // не удалось обновить SQL
                            TelegramBot.getInstance().sendBroadcastMsg(String.format("*%s.*\n %s\n\n%s", summary, diff, match_from_site.getLinkMatch()), match_from_site.getTeam_id());
                        }
                    }
                } catch (Exception e) {
                    System.out.format("\nНе удалось обработать матч %s\n Summary:%s\n Ошибка:%s", match_from_site.getLinkMatch(), summary, e.getMessage());
                }
            }
        }
    }
}
