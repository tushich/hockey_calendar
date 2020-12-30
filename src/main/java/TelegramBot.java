import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Properties;

public class TelegramBot extends TelegramLongPollingBot {

    // TODO Перевести пароли в безопасное хранилище
    private static String PROXY_HOST = "gilof.com" /* proxy host */;
    private static Integer PROXY_PORT = 21080 /* proxy port */;
    private static String PROXY_USER = "1835d413" /* proxy user */;
    private static String PROXY_PASSWORD = "94bc8ba7" /* proxy password */;

    private static String chat_IDs[] = {"298799539", "196469012"};

    private static TelegramBot instance;
    private static DefaultBotOptions botOptions = getBotOptions();

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
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    private static DefaultBotOptions getBotOptions()
    {
        // Create the Authenticator that will return auth's parameters for proxy authentication
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(PROXY_USER, PROXY_PASSWORD.toCharArray());
            }
        });

        ApiContextInitializer.init();

        // Set up Http proxy
        DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

        botOptions.setProxyHost(PROXY_HOST);
        botOptions.setProxyPort(PROXY_PORT);
        // Select proxy type: [HTTP|SOCKS4|SOCKS5] (default: NO_PROXY)
        botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);

        return botOptions;
    }

    /**
     * Метод для настройки сообщения и его отправки.
     * @param s Строка, которую необходимот отправить в качестве сообщения.
     */
    public synchronized void sendMsg(String s) {
        for (String chat_id : chat_IDs) {
            sendMsgDirect(chat_id, s);
        }

    }

    /**
     * Метод для настройки сообщения и его отправки.
     * @param chatId id чата
     * @param s Строка, которую необходимот отправить в качестве сообщения.
     */
    public synchronized void sendMsgDirect(String chatId, String s) {
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
        // TODO Добавить возможность пользователю подписываться и отписываться на оповещения
        // TODO Хранить ID чата пользователя в Базе данных. Сделать список рассылки.
        String message = update.getMessage().getText();
        sendMsgDirect(update.getMessage().getChatId().toString(), message);
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

        return new Resources().getResource("token_red_bears_bot");
        // TODO Сделать одного бота на все команды
    }

}
