import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.auth.http.HttpCredentialsAdapter;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.*;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CalenderGoogle {
    private static final String APPLICATION_NAME = Resources.getResource("APPLICATION_NAME");
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static Calendar service = null;
    // TODO Сделать командный календарь для каждой команды
    public String calendarID = Resources.getResource("calendarID");
    // Календарь штерна "ieesvcpisvro03mobsrnv54k5o@group.calendar.google.com";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String SERVICE_ACCOUNT_FILE_PATH = "src/main/resources/stern-calendar.json";

    public CalenderGoogle() throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.

        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(getCredentials()))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
    }

    private static GoogleCredentials getCredentials() throws IOException {

            // аутентификация на сервере черезх сервисный аккаунт
            InputStream is = new ByteArrayInputStream(Resources.getResource("google_credential").getBytes());
            return GoogleCredentials.fromStream(is)
                    .createScoped(SCOPES);
    }

    public Event getEvent(String eventId) throws IOException {
        return service.events().get(calendarID, eventId).execute();

    }

    public void updateEvent(String eventId, String Summary, DateTime startDateTime, DateTime endDateTime, String linkMatch, String protokolExist, String count) throws IOException {
        // https://developers.google.com/calendar/create-events
        boolean newEvent = false;
        Event event = new Event();
        Event old_event = null;
        try {
            old_event = this.getEvent(eventId);
        } catch (IOException e) {
            // не нашли событие
            newEvent = true;
        }

        event.setSummary(Summary);
        event.setStatus("confirmed");

        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Europe/Moscow");
        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Europe/Moscow");
        event.setEnd(end);

        EventReminder[] reminderOverrides = new EventReminder[] {
                new EventReminder().setMethod("popup").setMinutes(24 * 60),
        };

        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);

        JSONObject rootJSON = new JSONObject(); // создаем главный объект
        rootJSON.put("protokolExist", protokolExist);
        rootJSON.put("count", count);
        event.setDescription(rootJSON.toString()); // описание используем для хранения статуса наличия протокола матча + счета

        if(newEvent)
        {
            // создадим новое событие
            event.setId(eventId);
            service.events().insert(calendarID, event).execute();
            System.out.println("Новое событие");
            TelegramBot.getInstance().sendMsg(String.format("Добавлен новый матч:\n*%s*. Дата: %s\n%s", Summary, getDateString(event.getStart()), linkMatch));
        }
        else
        {
            // обновим событие
            String msg_text = "";
            boolean needToUpdate = false;
            if(!old_event.getStart().equals(event.getStart())) {
                needToUpdate = true;
                String newStringDate = startDateTime.equals(new DateTime(0))? "Пусто" : getDateString(event.getStart()); // Если пустая дата, то ставим пусто
                String oldStringDate = old_event.getStart().getDateTime().equals(new DateTime(0))? "Пусто" : getDateString(old_event.getStart());
                msg_text = String.format("\nИзменилась дата и время:\n%s -> \n%s", oldStringDate, newStringDate);
            }

            try {
                JSONObject oldJSON = new JSONObject(old_event.getDescription());
                if(!oldJSON.getString("protokolExist").equals(protokolExist)) {
                    needToUpdate = true;
                    msg_text = msg_text + "\nДобавлен протокол.";
                }
                if(!oldJSON.getString("count").equals(count)) {
                    needToUpdate = true;
                    msg_text = msg_text + "\nОбновлен счет:" + count;
                }
            }
            catch (JSONException e) {
                needToUpdate = true;
                msg_text = msg_text + "\nОбновлен счет. Данные по ссылке.";
            }

            if(!old_event.getSummary().equals(event.getSummary()))
            {
                needToUpdate = true;
                // TODO вытащить стадион, в параметр. Т.к. может быть изменен
                msg_text = String.format("%s\nИзменилось описание. \n%s -> \n%s ", msg_text, old_event.getSummary(), event.getSummary());
            }
            if(needToUpdate)
            {
                service.events().patch(calendarID, eventId, event).execute();
                System.out.println(old_event.getSummary() + msg_text);
                TelegramBot.getInstance().sendMsg(String.format("*%s.*\n %s\n\n%s", Summary, msg_text, linkMatch));
            }

        }

    }
    private String getDateString(EventDateTime dt)
    {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return df.format(new Date(dt.getDateTime().getValue()));

    }
}
