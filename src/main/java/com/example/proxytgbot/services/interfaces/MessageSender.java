package com.example.proxytgbot.services.interfaces;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface MessageSender {
    void sendMessage(Long chatId, String textToSend);

    void sendMessageWithButtons(Long chatId, String textToSend, InlineKeyboardMarkup markup);

    void sendErrorMessageByScheduler(Long chatId, String textToSend);

    void deleteMessage(Long chatId, Integer messageId);

    void showMainMenu(Long chatId);

    void showMainMenuForAdmin(Long chatId);

    void showDomainMenu(Long chatId);

    void showProxyMenu(Long chatId);

    void deleteDomainButton(Long chatId);

    void deleteGeoButton(Long chatId);

    void deleteDomain(Long chatId, String domain, Integer messageId);

    void deleteProxy(Long chatId, Long id, Integer messageId);

    void deleteGeo(Long chatId, Long id, Integer messageId);

    void deleteDomainConfirmation(Long chatId, String domain);

    void deleteProxyConfirmation(Long chatId, Long id);

    void deleteGeoConfirmation(Long chatId, Long id);

    void createKey(Long chatId);

    void showGeo(Long chatId);

    void deleteProxyButton(Long chatId);
    void resetProxy(Long chatId);

    void addGeoMessage(Long chatId);

    void addDomainMessage(Long chatId);
    void addProxyMessage(Long chatId);

    boolean addGeo(Long chatId, String geo);

    boolean addDomain(Long chatId, String domain, Long geoId);
    boolean addProxy(Long chatId, String proxy, Long geoId);

    void chooseGeoForDomain(Long chatId);
    void chooseGeoForProxy(Long chatId);
    void makeAdmin(Long chatId);
}
