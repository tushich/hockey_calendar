import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

public class TelegramBot extends TelegramLongPollingBot {

    private static TelegramBot instance;
    private static final String location = Resources.getResource("location");
    private static final String admin_chat_id = Resources.getResource("tushich_id_chat");

    public static TelegramBot getInstance() { // #3
        if (instance == null) {        //если объект еще не создан
            instance = new TelegramBot();    //создать новый объект
        }
        return instance;        // вернуть ранее созданный объект
    }

    public void startListen(){
        //Запускает бота на прослушку получаемых сообщений
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            System.out.format("\nОшибка подключения телеграм:%s", e.getMessage());
        }
    }

    /**
     * Метод отправляет сообщения по списку рассылки из БД
     * @param s Строка, которую необходимот отправить в качестве сообщения.
     */
    public synchronized void sendMsg(String s) {

        if(location.equals("prod")) {
            List<String> list = DataBase.getUsersList(Resources.getResource("teamName"));
            for (String chat_id : list) {
                sendMsgDirect(chat_id, s);
            }
        }
        else // если шлёт в тесте, то отправялть админу 1 раз
        {
            sendMsgDirect(admin_chat_id, s);
        }
    }

    /**
     * Метод для отправки сообщения.
     * @param chatId id чата
     * @param s Строка, которую необходимот отправить в качестве сообщения.
     */
    public synchronized void sendMsgDirect(String chatId, String s) {
        if(location.equals("test"))
        {
            chatId = admin_chat_id;
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(s);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод для приема сообщений.
     * @param update Содержит сообщение от пользователя.
     */
    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if(message.getText().equals("/start")){
            boolean allIsOk = DataBase.addUser(message.getChatId().toString(), Resources.getResource("teamName"), message.getFrom().getFirstName() + " " + message.getFrom().getLastName(), message.getFrom().getUserName());
            String msg;
            if(allIsOk) msg = "Добро пожаловать в чат оповещений по играм команд c сайтов СПБХЛ и ФХСПб\n" +
                    "https://www.fhspb.ru/" + "https://spbhl.ru/\n" +
                    "\nПри изменениях на сайтах СПБХЛ, вы автоматически получите оповещение." +
                    "\nЧтобы прекратить получать сообщения введите '/stop'" +
                    "\nКалендарь всех игр находится тут: https://calendar.google.com/calendar/u/0?cid=OW9waHNjamMwMHNzb25qNG80a2QxdGYwYThAZ3JvdXAuY2FsZW5kYXIuZ29vZ2xlLmNvbQ";
            else msg = "Произошла ошибка. Не удалось добавить пользователя.";
            sendMsgDirect(update.getMessage().getChatId().toString(), msg);

        }
        else if(message.getText().equals("/stop"))
        {
            boolean allIsOk = DataBase.delUser(update.getMessage().getChatId().toString(), Resources.getResource("teamName"));
            String msg;
            if(allIsOk) msg = "Пока и жаль! Чтобы начать общение заново, необходимо написать '/start'";
            else msg = "Произошла ошибка. Не удалось удалить пользователя.";
            sendMsgDirect(update.getMessage().getChatId().toString(), msg);

        }
        else
        {
            sendMsgDirect(update.getMessage().getChatId().toString(), "Ошибка: Неизвестная команда чата");
        }

    }

    /**
     * Метод возвращает имя бота, указанное при регистрации.
     * @return имя бота
     */
    @Override
    public String getBotUsername() {
        return "Red_Bears_Bot";
        //"Stern_Calendar_Bot";
    }

    /**
     * Метод возвращает token бота для связи с сервером Telegram
     * @return token для бота
     */
    @Override
    public String getBotToken() {
        return Resources.getResource("token_hockey_notification_bot");
    }

}