package org.example;

import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.Map;

import static org.example.Constants.START_TEXT;
import static org.example.UserState.AWAITING_NIE;
import static org.example.UserState.NIE_ADDED;

public class AppointmentResponseHandler {
    private final SilentSender sender;
    private final Map<Long, UserState> chatStates;
    private final Map<Long, String> chatNiePair;

    public AppointmentResponseHandler(SilentSender sender, DBContext db) {
        this.sender = sender;
        chatStates = db.getMap(Constants.CHAT_STATES);
        chatNiePair = db.getMap(Constants.CHAT_NIE);
    }

    public void replyToStart(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(START_TEXT);
        sender.execute(message);
        chatStates.put(chatId, AWAITING_NIE);
    }

    public void nieFound(String nie) {
        chatNiePair.entrySet().stream().filter(i -> i.getValue().equals(nie))
                .forEach(i -> replyWithMessage(i.getKey(), "NIE: " + nie + " is found"));
    }

    public void replyToButtons(long chatId, Message message) {
        if (message.getText().equalsIgnoreCase("/stop")) {
            stopChat(chatId);
        }

        switch (chatStates.get(chatId)) {
            case AWAITING_NIE: {
                replyToNieInput(chatId, message);
                break;
            }

            case NIE_ADDED: {
                replyToNieIsAlreadyAdded(chatId);
                break;

            }
            default: {
                unexpectedMessage(chatId);
            }
        }
    }

    private void unexpectedMessage(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("I did not expect that.");
        sender.execute(sendMessage);
    }

    private void stopChat(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Chat is stopped.");
        chatStates.remove(chatId);
        chatNiePair.remove(chatId);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sender.execute(sendMessage);
    }


    private void replyWithMessage(long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sender.execute(sendMessage);
    }

    private void replyToNieIsAlreadyAdded(long chatId) {
        String nie = chatNiePair.get(chatId);
        replyWithMessage(chatId, "You NIE " + nie + " is already added");
    }

    private void replyToNieInput(long chatId, Message message) {
        chatNiePair.put(chatId, message.getText());
        chatStates.put(chatId, NIE_ADDED);
        replyWithMessage(chatId, "You NIE is " + message.getText() + ". We will notify you.");
    }

    public boolean userIsActive(Long chatId) {
        return chatStates.containsKey(chatId);
    }
}