import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.List;

public class TelegramBot extends TelegramLongPollingBot {

    private static final String PROXY_HOST = Resources.getResource("PROXY_HOST");
    private static final Integer PROXY_PORT = Integer.getInteger(Resources.getResource("PROXY_PORT"));
    private static final String PROXY_USER = Resources.getResource("PROXY_USER");
    private static final String PROXY_PASSWORD = Resources.getResource("PROXY_PASSWORD");
    private static final Boolean use_proxy = Resources.getResource("use_proxy") != null
            & Boolean.getBoolean(Resources.getResource("use_proxy"));
    private static TelegramBot instance;
    private static final DefaultBotOptions botOptions = getBotOptions();
    private static final String location = Resources.getResource("location");
    private static final String admin_chat_id = Resources.getResource("tushich_id_chat");
    private TelegramBot() {
        super(botOptions);
    }

    public static TelegramBot getInstance() { // #3
        if (instance == null) {        //если объект еще не создан
            instance = new TelegramBot();    //создать новый объект
        }
        return instance;        // вернуть ранее созданный объект
    }

    public void startListen(){
        //Запускает бота на прослушку получаемых сообщений
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(this);
            // TODO 0. Починить. Бот мешает сам себе получать апдейты от телеги. ВОзможно перейти на вебхуки
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    private static DefaultBotOptions getBotOptions()
    {

        DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
        if(use_proxy) {
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(PROXY_USER, PROXY_PASSWORD.toCharArray());
                }
            });
            ApiContextInitializer.init();
            botOptions.setProxyHost(PROXY_HOST);
            botOptions.setProxyPort(PROXY_PORT);
            // Select proxy type: [HTTP|SOCKS4|SOCKS5] (default: NO_PROXY)
            botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
        }
        return botOptions;
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

        return Resources.getResource("token_red_bears_bot");
        // TODO 0 Сделать нового бота. Одного для всех компнд
    }

}
