import java.util.List;
import java.util.Map;

public class hockey_calendar {
    private static final String location = Resources.getResource("location");
    public static void main(String[] args) {

        // Процедуры первоначального заполнения. Запускать при первом запуске в окружении
        /*
        DataBase.createTableMatches();
        DataBase.createTableUsers();
        DataBase.createTableSubscriptions();

        */
        if (location.equals("prod")) {
            TelegramBot.getInstance().startListen();
        }

        // TODO при подключении новой команды, не выдавать спам по загрузке матчей.
        // TODO 3. Сравнивать состав команд. Могут поменять команду а ID оставить старый т.е. в боте больше матчей чем на сайте

        Map<String, Team> teams = DataBase.getTeams();
        for (Map.Entry<String, Team> entry : teams.entrySet()) {
            Team team = entry.getValue();
            List<Match> matchTable = Site.getMatchTable(team.teamId, team.siteId);

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
                        // TODO Сделать напоминалку про матч за настраиваемое количество часов(168ч) и ссылку на видео присылать за 2 часа(Настраиваемо)
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
                    return; // закончим работать, т.к. скорее всего есть проблема с подключениями
                    // TODO Сделать обработку ошибки подключения к SQL
                }
            }
        }
    }
}

