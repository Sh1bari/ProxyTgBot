package com.example.proxytgbot.services.interfaces;

public interface SecurityService {
    boolean hasKeyByChatId(Long chatId, boolean sendMessage);

    void saveNewUser(Long chatId);

    void connectKeyToUser(Long chatId, String msg);
}
