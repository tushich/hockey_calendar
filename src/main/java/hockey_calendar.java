import java.util.List;

public class hockey_calendar {

    public static void main(String[] args) {

        // Процедуры первоначального заполнения. Запускать при первом запуске в окружении
        /*
        DataBase.createTableMatches();
        DataBase.createTableUsers();
        DataBase.createTableSubscriptions();
        */

        /*
        // Временные процедурки
        try {
            DataBase.executeSQLUpdate("UPDATE matches SET Count = '0' WHERE  matchID = '5825v90220'", null);
        } catch (Exception e) {
            // Ошиболнька
        }

        try {
            DataBase.executeSQLUpdate("ALTER TABLE matches ADD site_id VARCHAR(40)", null);
        } catch (SQLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        */
        TelegramBot.getInstance().startListen();

        // TODO 0. Сделать подписку на оповещения по командам из чата. Привязку хранить в списке пользователей.
        // TODO 1. Получать список команд и списка подписок

        ElementMass[] teamsArray = new ElementMass[3];
        teamsArray[1] = new ElementMass("spbhl.ru", Resources.getResource("teamIdSpbhl_red_bears_main"));
        teamsArray[2] = new ElementMass("spbhl.ru", Resources.getResource("teamIdSpbhl_red_bears_farm"));
        teamsArray[3] = new ElementMass("fhspb.ru", Resources.getResource("team_red_bears_2009"));
        teamsArray[4] = new ElementMass("fhspb.ru", Resources.getResource("team_red_bears_2011"));
        teamsArray[5] = new ElementMass("fhspb.ru", Resources.getResource("team_red_bears_2012"));

        // TODO при подключении новой команды, не выдавать спам по загрузке матчей.

        // TODO 3. Сравнивать состав команд. Могут поменять команду а ID оставить старый т.е. в боте больше матчей чем на сайте

        // CalenderGoogle calender = new CalenderGoogle();

        for (ElementMass elementMass : teamsArray) {

                List<Match> matchTable = siteSPBHL.getMatchTable(elementMass.teamId, elementMass.siteId);

                for (Match match_from_site : matchTable) {

                    String summary = "*" + match_from_site.getTeams() + "*\n\nСтадион: *" + match_from_site.getStadium() + "*\nТурнир: *" + match_from_site.getTournament() + "*";

                    try {
                        Match match_from_dataBase = DataBase.getMatch(match_from_site.getMatchID());
                        if (match_from_dataBase.isEmpty()) // Если пустой, то создадим новый матч
                        {
                            if (!DataBase.addMatch(match_from_site))
                                continue; // если не добавили, то погнали дальше
                            TelegramBot.getInstance().sendBroadcastMsg(String.format("Добавлен новый матч:\n%s. \nДата: *%s*\n%s", summary, match_from_site.getStringStartDateTime(), match_from_site.getLinkMatch()), match_from_site.getTeam_id(), match_from_site.getSiteID());
                        } else // Матч уже есть в базеДанных, значит надо найти различия. Обновить. и Сообщить об обновлении.
                        {
                            String diff = match_from_dataBase.compare(match_from_site);
                            if (!diff.isEmpty()) {
                                if (!DataBase.updateMatch(match_from_site))
                                    continue; // не удалось обновить SQL
                                TelegramBot.getInstance().sendBroadcastMsg(String.format("%s.\n %s\n\n%s", summary, diff, match_from_site.getLinkMatch()), match_from_site.getTeam_id(), match_from_site.getSiteID());
                            }
                        }
                    } catch (Exception e) {
                        String errText = String.format("\nНе удалось обработать матч %s\n Summary:%s\n Ошибка:%s", match_from_site.getLinkMatch(), summary, e.getMessage());
                        System.out.format(errText);
                        TelegramBot.getInstance().sendMsgToAdmin(errText);
                    }
                }
            }
        }
    }

class ElementMass {
    public String teamId, siteId;
    public ElementMass(String siteId, String teamId) {
        this.siteId = siteId;
        this.teamId = teamId;
    }
}

