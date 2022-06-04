import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class hockey_calendar {

    public static void main(String[] args) throws GeneralSecurityException, IOException {

        TelegramBot.getInstance().startListen();

        // System.setProperty("java.net.useSystemProxies", "true");

        // TODO 2. Сейчас команды только 2. Добавляем всех в один массив. Надо сделать красивше.
        String[] teamIDArray = new String[2];
        teamIDArray[0] = Resources.getResource("teamIdSpbhl");
        teamIDArray[1] = Resources.getResource("teamIdSpbhl_2");

        // TODO 1. Перевести хранение данных событий в базу данных из календаря.
        // TODO 3. СРавнивать список матчей календарь - сайт и в обратном порядке. Если в матче поменяли нашу команду, то бот не удаляет матч.
        // TODO 4. Сделать отправку ошибок в чат Максиму Т
        CalenderGoogle calender = new CalenderGoogle();
        for (String teamID : teamIDArray) {

            List<Match> tableCalender = siteSPBHL.getMatchTable(String.format("http://spbhl.ru/Schedule?TeamID=%s", teamID));

            for (Match match_from_site : tableCalender) {

                String summary = match_from_site.getTeams() + "\nСтадион:" + match_from_site.getStadium() + " Турнир:" + match_from_site.getTournament();


                Match match_from_dataBase = DataBase.getMatch(match_from_site.getMatchID());
                if(match_from_dataBase.isEmpty()) // Если пустой, то создадим овый матч
                {
                    DataBase.addMatch(match_from_site);
                    TelegramBot.getInstance().sendMsg(String.format("Добавлен новый матч:\n*%s*. Дата: %s\n%s", summary, match_from_site.getStartDateTime(), match_from_site.getLinkMatch()));
                }
                else // Матч уже есть в базеДанных, значит надо найти различия. Обновить. и Сообщить об обновлении.
                {
                    String diff = match_from_site.compare(match_from_dataBase);
                    if (!diff.isEmpty())
                    {
                        TelegramBot.getInstance().sendMsg(String.format("*%s.*\n %s\n\n%s", summary, diff, match_from_site.getLinkMatch()));
                    }
                    DataBase.updateMatch(match_from_site);
                }




                /*String summary = "";
                DateTime startDateTime = new DateTime(0);
                DateTime endDateTime = new DateTime(0);

                try {
                    summary = tableCalender.get(i).get("teams") + "\nСтадион:" + tableCalender.get(i).get("Stadium") + " Турнир:" + tableCalender.get(i).get("Tournament");
                    String id = tableCalender.get(i).get("matchID");
                    try { // в поле с датой может быть что угодно. Например слово "Перенос"
                        // TODO Перенести фаормирвоание дат в класс siteSPBHL.java
                        String startDate = tableCalender.get(i).get("startDate");  // формат 21:00:00
                        String startTime = tableCalender.get(i).get("startTime") + ":00";
                        startDate = startDate.substring(9, 13) + "-" + startDate.substring(6, 8) + "-" + startDate.substring(3, 5);
                        startDateTime = new DateTime(startDate + "T" + startTime + "+03:00");
                        endDateTime = new DateTime(startDateTime.getValue() + (60000 * 75));
                    }
                    catch (Exception e) {
                        //System.out.format("\nНе удалось обработать стартовую дату для строки %d\n Summary:%s\n Ошибка:%s", i, summary,  e.getMessage());
                        //Ничего не делаем. Отправляем пустую дату.
                    }

                    String protokolExist = tableCalender.get(i).get("protokolExist") ;
                    String linkMatch =  tableCalender.get(i).get("linkMatch") ;
                    String count = tableCalender.get(i).get("count") ;
                    calender.updateEvent(id, summary, startDateTime, endDateTime, linkMatch, protokolExist, count);

                } catch (Exception e) {
                    System.out.format("\nНе удалось обработать строку %d\n Summary:%s\n Ошибка:%s", i, summary,  e.getMessage());
                }
                 */
            }
        }
    }
}
