import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class stern_calender {

    public static void main(String[] args) throws GeneralSecurityException, IOException {

        //TelegramBot.getInstance().sendMsg("196469012","Бот запущен");

        System.setProperty("java.net.useSystemProxies", "true");

        // TODO вынести ID команды в настроки ПО
        TwoDimentionalArrayList<String> tableCalender = siteSPBHL.getTable("http://spbhl.ru/Schedule?TeamID=1d861135-80ca-4108-bc71-d27a64950a65");

        // TODO Перевести хранение данных событий в базу данных.
        CalenderGoogle calender = new CalenderGoogle();
        for (int i = 1; i < tableCalender.size(); i++) {
            String summary = "";
            DateTime startDateTime = new DateTime(0);
            DateTime endDateTime = new DateTime(0);
            try {
                summary = tableCalender.getCellValue(i, 5) + "\nСтадион:" + tableCalender.getCellValue(i, 4) + " Турнир:" + tableCalender.getCellValue(i, 0);
                String id = (String) tableCalender.getCellValue(i, 9);
                try { // в поле с датой может быть что угодно. Например слово "Перенос"
                    // TODO Перенести фаормирвоание дат в класс siteSPBHL.java
                    String startDate = (String) tableCalender.getCellValue(i, 2);  // формат 21:00:00
                    String startTime = tableCalender.getCellValue(i, 3) + ":00";
                    startDate = startDate.substring(9, 13) + "-" + startDate.substring(6, 8) + "-" + startDate.substring(3, 5);
                    startDateTime = new DateTime(startDate + "T" + startTime + "+03:00");
                    endDateTime = new DateTime(startDateTime.getValue() + (60000 * 75));
                }
                catch (Exception e) {
                    //System.out.format("\nНе удалось обработать стартовую дату для строки %d\n Summary:%s\n Ошибка:%s", i, summary,  e.getMessage());
                    //Ничего не делаем. Отправляем пустую дату.
                }

                String protokolExist = (String) tableCalender.getCellValue(i, 7);
                String linkMatch =  (String) tableCalender.getCellValue(i, 8);
                String count = (String) tableCalender.getCellValue(i, 6);
                calender.updateEvent(id, summary, startDateTime, endDateTime, linkMatch, protokolExist, count);
                break;
            } catch (Exception e) {
                System.out.format("\nНе удалось обработать строку %d\n Summary:%s\n Ошибка:%s", i, summary,  e.getMessage());
            }
        }

        //TelegramBot.getInstance().sendMsg("196469012","Бот выключен");
    }



}
