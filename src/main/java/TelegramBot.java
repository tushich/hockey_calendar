import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TelegramBot extends TelegramLongPollingBot {

    private static TelegramBot instance;
    private static final String location = Resources.getResource("location");
    private static final String admin_chat_id = Resources.getResource("admin_chat_id");

    public static TelegramBot getInstance() { // #3
        if (instance == null) {        //если объект еще не создан
            instance = new TelegramBot();    //создать новый объект
        }
        return instance;        // вернуть ранее созданный объект
    }

    public void startListen() {
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
     *
     * @param message Строка, которую необходимот отправить в качестве сообщения.
     */
    public void sendBroadcastMsg(String message, String team_id, String site_id) {

        if (location.equals("prod")) {
            List<String> list = DataBase.getUsersListSubscribedForTeam(team_id, site_id);
            for (String chat_id : list) {
                sendMsgDirect(chat_id, message, null);
            }
        } else // если шлёт в тесте, то отправялть админу 1 раз
        {
            sendMsgDirect(admin_chat_id, message, null);
        }
    }

    /**
     * Метод для отправки сообщения.
     *
     * @param chatId id чата
     * @param s      Строка, которую необходимот отправить в качестве сообщения.
     */
    public void sendMsgDirect(String chatId, String s, ReplyKeyboard buttons) {
        if (location.equals("test")) {
            chatId = admin_chat_id;
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        if(buttons != null)
            sendMessage.setReplyMarkup(buttons);
        sendMessage.setChatId(chatId);

        s = s.replace("_", "\\_")
                .replace("[", "\\[")
                .replace("`", "\\`");

        sendMessage.setText(s);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMsgToAdmin(String s) {
        sendMsgDirect(admin_chat_id, s, null);
    }

    /**
     * Метод для приема сообщений.
     *
     * @param update Содержит сообщение от пользователя.
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().hasText()) {

                Message message = update.getMessage();
                if (message.getText().equals("/start")) {
                    // TODO добавить запись подписки в таблицу подписок
                    boolean allIsOk = DataBase.addUser(message.getChatId().toString(), message.getFrom().getFirstName() + " " + message.getFrom().getLastName(), message.getFrom().getUserName());
                    String msg;
                    if (allIsOk) {
                        msg = "Добро пожаловать в чат оповещений по играм команд c сайтов СПБХЛ и ФХСПб\n" +
                                "https://www.fhspb.ru/" + "\nhttps://spbhl.ru/\n" +
                                "\nПри изменениях на сайтах, вы автоматически получите оповещение." +
                                "\nЧтобы прекратить получать сообщения введите '/stop'";
                        // "\nКалендарь всех игр находится тут: https://calendar.google.com/calendar/u/0?cid=OW9waHNjamMwMHNzb25qNG80a2QxdGYwYThAZ3JvdXAuY2FsZW5kYXIuZ29vZ2xlLmNvbQ"
                        sendMsgDirect(update.getMessage().getChatId().toString(), msg, getKeyboardForChat(Arrays.asList("Подписаться", "Отписаться")));

                    } else {
                        msg = "Произошла ошибка. Не удалось добавить пользователя.";
                        sendMsgDirect(update.getMessage().getChatId().toString(), msg, null);
                    }

                } else if (message.getText().equals("/stop")) {
                    boolean allIsOk = DataBase.delUser(update.getMessage().getChatId().toString());
                    String msg;
                    if (allIsOk) msg = "Пока и жаль! \nВаши подписки очищены. \nЧтобы начать общение заново, необходимо написать '/start'";
                    else msg = "Произошла ошибка. Не удалось удалить пользователя.";
                    sendMsgDirect(update.getMessage().getChatId().toString(), msg, null);
                    // TODO Удалять подписки пользователя при отписке

                } else if (message.getText().equals("Подписаться")) {
                    sendMsgDirect(update.getMessage().getChatId().toString(),
                            "Выберите команду для *Подписки*:",
                            getInlineKeyboard(DataBase.getTeams(), "Подписаться"));
                    // TODO Сделать поиск команд для подписки. Т.е. пользователь вводит название и их ищем на сайте
                    // TODO Добавить шаг выбора сайта

                } else if (message.getText().equals("Отписаться")) {
                    sendMsgDirect(update.getMessage().getChatId().toString(),
                            "Выберите команду для *Отписки*:",
                            getInlineKeyboard(DataBase.getTeams(), "Отписаться"));
                    // TODO Брать список для отписки из БД
                }
                else {
                    sendMsgDirect(update.getMessage().getChatId().toString(), "Ошибка: Неизвестная команда чата", null);
                }
            }
        } else if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getData().equals("Подписаться")) {
                Team team = DataBase.getTeams().get(update.getCallbackQuery().getMessage());
                DataBase.addSubscription(update.getCallbackQuery().getMessage().getChatId().toString(), team.teamId, team.siteId);
            } else if (update.getCallbackQuery().getData().equals("Отписаться")) {
                Team team = DataBase.getTeams().get(update.getCallbackQuery().getMessage());
                DataBase.delSubscription(update.getCallbackQuery().getMessage().getChatId().toString(), team.teamId, team.siteId);
            }
        }
    }

    private InlineKeyboardMarkup getInlineKeyboard(Map<String,Team> teams, String callBackText) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for(Map.Entry<String, Team> team : teams.entrySet()){
            List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(team.getKey());
            inlineKeyboardButton.setCallbackData(callBackText);
            keyboardButtonsRow.add(inlineKeyboardButton);
            rowList.add(keyboardButtonsRow);
        }

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private static ReplyKeyboardMarkup getKeyboardForChat(List<String> buttons)
    {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        for (String button : buttons) {
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.add(button);
            keyboard.add(keyboardRow);
        }
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    /**
     * Метод возвращает имя бота, указанное при регистрации.
     *
     * @return имя бота
     */
    @Override
    public String getBotUsername() {
        return "Red_Bears_Bot";
        //"Stern_Calendar_Bot";
    }

    /**
     * Метод возвращает token бота для связи с сервером Telegram
     *
     * @return token для бота
     */
    @Override
    public String getBotToken() {
        return Resources.getResource("token_hockey_notification_bot");
    }

}