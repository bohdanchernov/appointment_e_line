package org.example;


import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

//@Component
public class AppointmentNotifyBot extends TelegramLongPollingBot {
//    @Value("app.telegram.bot.token")
//    private String BOT_TOKEN;
//    @Value("app.telegram.bot.name")
//    private String BOT_USER_NAME;

    private String botName;

    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            SendMessage message = new SendMessage(); // Create a message object object
            message.setChatId(chat_id);
            message.setText(message_text);
            try {
                execute(message); // Sending our message object to user
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }


    public AppointmentNotifyBot(String botToken, String botName) {
        super(botToken);
        this.botName = botName;
    }

    @Override
    public String getBotUsername() {
        // Return bot username
        // If bot username is @MyAmazingBot, it must return 'MyAmazingBot'
        return botName;
    }

//    @Override
//    public String getBotToken() {
//        // Return bot token from BotFather
//        return BOT_TOKEN;
//    }
}
