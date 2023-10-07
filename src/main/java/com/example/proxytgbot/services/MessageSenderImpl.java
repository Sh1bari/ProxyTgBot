package com.example.proxytgbot.services;

import com.example.proxytgbot.config.BotConfig;
import com.example.proxytgbot.services.interfaces.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MessageSenderImpl extends TelegramLongPollingBot implements MessageSender {


    @Override
    public void sendMessage(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("Неудалось отправить сообщение в чат " + chatId);
        }
    }

    @Override
    public void sendMessageWithButtons(Long chatId, String textToSend, InlineKeyboardMarkup markup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        sendMessage.setReplyMarkup(markup);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("Неудалось отправить сообщение в чат " + chatId);
        }
    }

    @Override
    public void sendErrorMessageByScheduler(Long chatId, String textToSend) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(createInlineKeyboardButtonList(createInlineKeyboardButton("Решено", "SOLVED_PROBLEM", null)));
        markup.setKeyboard(rowsInline);
        sendMessageWithButtons(chatId, textToSend, markup);
    }

    @Override
    public void deleteMessage(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage(chatId.toString(), messageId);
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            System.out.println("Неудалось отправить сообщение в чат " + chatId);
        }
    }

    private List<InlineKeyboardButton> createInlineKeyboardButtonList(InlineKeyboardButton... buttons){
        return new ArrayList<>(Arrays.asList(buttons));
    }
    private InlineKeyboardButton createInlineKeyboardButton(String text, String callbackData, String url){
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(text);
        inlineKeyboardButton.setUrl(url);
        inlineKeyboardButton.setCallbackData(callbackData);
        return inlineKeyboardButton;
    }

    @Autowired
    private BotConfig botConfig;

    @Override
    public void onUpdateReceived(Update update) {
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }
}
