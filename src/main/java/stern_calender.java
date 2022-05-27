import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class stern_calender {

    public static void main(String[] args) throws GeneralSecurityException, IOException {

        TelegramBot.getInstance().startListen();

        System.setProperty("java.net.useSystemProxies", "true");

        // TODO СЕйчас команды только 2. Добавляем всех в один массив. Надо сделать красивше.
        String[] teamIDArray = new String[2];
        teamIDArray[0] = Resources.getResource("teamIdSpbhl");
        teamIDArray[1] = Resources.getResource("teamIdSpbhl_2");

        // TODO Перевести хранение данных событий в базу данных из календаря.
        // TODO СРавнивать список матчей календарь - сайт и в обратном порядке. Если в матче поменяли нашу команду, то бот не удаляет матч.
        // TODO Сделать приявязку полей с сайта по наименованию колонок, а не по ID
        // TODO Сделать отправку ошибок в чат Максиму Т
        CalenderGoogle calender = new CalenderGoogle();
        for (String teamID : teamIDArray) {

            TwoDimentionalArrayList<String> tableCalender = siteSPBHL.getTable(String.format("http://spbhl.ru/Schedule?TeamID=%s", teamID));

            for (int i = 1; i < tableCalender.size(); i++) {
                String summary = "";
                DateTime startDateTime = new DateTime(0);
                DateTime endDateTime = new DateTime(0);
                try {
                    summary = tableCalender.getCellValue(i, 6) + "\nСтадион:" + tableCalender.getCellValue(i, 5) + " Турнир:" + tableCalender.getCellValue(i, 0);
                    String id = (String) tableCalender.getCellValue(i, 10);
                    try { // в поле с датой может быть что угодно. Например слово "Перенос"
                        // TODO Перенести фаормирвоание дат в класс siteSPBHL.java
                        String startDate = (String) tableCalender.getCellValue(i, 3);  // формат 21:00:00
                        String startTime = tableCalender.getCellValue(i, 4) + ":00";
                        startDate = startDate.substring(9, 13) + "-" + startDate.substring(6, 8) + "-" + startDate.substring(3, 5);
                        startDateTime = new DateTime(startDate + "T" + startTime + "+03:00");
                        endDateTime = new DateTime(startDateTime.getValue() + (60000 * 75));
                    }
                    catch (Exception e) {
                        //System.out.format("\nНе удалось обработать стартовую дату для строки %d\n Summary:%s\n Ошибка:%s", i, summary,  e.getMessage());
                        //Ничего не делаем. Отправляем пустую дату.
                    }

                    String protokolExist = (String) tableCalender.getCellValue(i, 8);
                    String linkMatch =  (String) tableCalender.getCellValue(i, 9);
                    String count = (String) tableCalender.getCellValue(i, 7);
                    calender.updateEvent(id, summary, startDateTime, endDateTime, linkMatch, protokolExist, count);
                } catch (Exception e) {
                    System.out.format("\nНе удалось обработать строку %d\n Summary:%s\n Ошибка:%s", i, summary,  e.getMessage());
                }
            }
        }
    }
}
