package com.example.proxytgbot.services.interfaces;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface MessageSender {
    void sendMessage(Long chatId, String textToSend);
    void sendMessageWithButtons(Long chatId, String textToSend, InlineKeyboardMarkup markup);

    void sendErrorMessageByScheduler(Long chatId, String textToSend);

    void deleteMessage(Long chatId, Integer messageId);

}
