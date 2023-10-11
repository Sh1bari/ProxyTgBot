package com.example.proxytgbot.telegram;


import com.example.proxytgbot.config.BotConfig;
import com.example.proxytgbot.models.enums.Role;
import com.example.proxytgbot.models.enums.UserState;
import com.example.proxytgbot.repositories.UserRepo;
import com.example.proxytgbot.services.interfaces.MessageSender;
import com.example.proxytgbot.services.interfaces.SecurityService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

@Component
@AllArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private BotConfig botConfig;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private MessageSender messageSender;

    private Map<Long, UserState> stateMap;
    private Map<Long, Long> addDomainMap;
    private Map<Long, Long> addProxyMap;
    @Autowired
    private UserRepo userRepo;

    @Override
    public void onUpdateReceived(Update update) {
        long chatId;
        try {
            chatId = update.getMessage().getChatId();
        } catch (Exception e) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }
        securityService.saveNewUser(chatId);

        if (update.hasMessage() &&
                update.getMessage().hasText() &&
                !securityService.hasKeyByChatId(chatId, false) &&
                update.getMessage().getText().startsWith("/key")) {
            String messageText = update.getMessage().getText();
            securityService.connectKeyToUser(chatId, messageText);

        } else if (securityService.hasKeyByChatId(chatId, true)) {

            if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                if (messageText.equals("/menu")) {
                    stateMap.remove(chatId);
                    Role role = userRepo.findUserByTelegramChatId(chatId).getRole();
                    switch (role) {
                        case USER -> messageSender.showMainMenu(chatId);
                        case ADMIN -> messageSender.showMainMenuForAdmin(chatId);
                    }
                } else if (stateMap.get(chatId) == UserState.ADD_GEO) {
                    if (messageSender.addGeo(chatId, messageText)) {
                        resetMap(chatId);
                    }
                } else if (stateMap.get(chatId) == UserState.ADD_DOMAIN) {
                    if (messageSender.addDomain(chatId, messageText, addDomainMap.get(chatId))) {
                        resetMap(chatId);
                    }
                } else if (stateMap.get(chatId) == UserState.ADD_PROXY) {
                    if (messageSender.addProxy(chatId, messageText, addProxyMap.get(chatId))) {
                        resetMap(chatId);
                    }
                }

            } else if (update.hasCallbackQuery()) {
                String callData = update.getCallbackQuery().getData();
                int messageId = update.getCallbackQuery().getMessage().getMessageId();
                String inlineMessageId = update.getCallbackQuery().getInlineMessageId();
                chatId = update.getCallbackQuery().getMessage().getChatId();
                if (callData.equals("SOLVED_PROBLEM")) {
                    messageSender.deleteMessage(chatId, messageId);
                } else if (callData.equals("DOMAIN_MENU")) {
                    resetMap(chatId);
                    messageSender.showDomainMenu(chatId);
                } else if (callData.equals("PROXY_MENU")) {
                    resetMap(chatId);
                    messageSender.showProxyMenu(chatId);
                } else if (callData.equals("ADD_DOMAINS")) {
                    resetMap(chatId);
                    messageSender.chooseGeoForDomain(chatId);
                } else if (callData.equals("ADD_PROXY")) {
                    resetMap(chatId);
                    messageSender.chooseGeoForProxy(chatId);
                } else if (callData.equals("SHOW_MENU")) {
                    resetMap(chatId);
                    Role role = userRepo.findUserByTelegramChatId(chatId).getRole();
                    switch (role) {
                        case USER -> messageSender.showMainMenu(chatId);
                        case ADMIN -> messageSender.showMainMenuForAdmin(chatId);
                    }
                } else if (callData.equals("DELETE_DOMAIN")) {
                    resetMap(chatId);
                    messageSender.deleteDomainButton(chatId);
                } else if (callData.startsWith("DELETE_DOMAIN_CONF_")) {
                    resetMap(chatId);
                    String domain = "https://" + callData.substring(19);
                    messageSender.deleteDomainConfirmation(chatId, domain);
                } else if (callData.startsWith("DELETE_DOMAIN_ACCEPT_")) {
                    resetMap(chatId);
                    String domain = callData.substring(21);
                    messageSender.deleteDomain(chatId, domain, messageId);
                } else if (callData.equals("CREATE_KEY")) {
                    resetMap(chatId);
                    messageSender.createKey(chatId);
                } else if (callData.equals("SHOW_GEO")) {
                    resetMap(chatId);
                    messageSender.showGeo(chatId);
                } else if (callData.equals("DELETE_PROXY")) {
                    resetMap(chatId);
                    messageSender.deleteProxyButton(chatId);
                } else if (callData.startsWith("DELETE_PROXY_CONF_")) {
                    resetMap(chatId);
                    Long id = Long.parseLong(callData.substring(18));
                    messageSender.deleteProxyConfirmation(chatId, id);
                } else if (callData.startsWith("DELETE_PROXY_ACCEPT_")) {
                    resetMap(chatId);
                    Long id = Long.parseLong(callData.substring(20));
                    messageSender.deleteProxy(chatId, id, messageId);
                } else if (callData.equals("DELETE_GEO")) {
                    resetMap(chatId);
                    messageSender.deleteGeoButton(chatId);
                } else if (callData.startsWith("DELETE_GEO_CONF_")) {
                    resetMap(chatId);
                    Long id = Long.parseLong(callData.substring(16));
                    messageSender.deleteGeoConfirmation(chatId, id);
                } else if (callData.startsWith("DELETE_GEO_ACCEPT_")) {
                    resetMap(chatId);
                    Long id = Long.parseLong(callData.substring(18));
                    messageSender.deleteGeo(chatId, id, messageId);
                } else if (callData.equals("ADD_GEO")) {
                    stateMap.put(chatId, UserState.ADD_GEO);
                    messageSender.addGeoMessage(chatId);
                } else if (callData.startsWith("ADD_DOMAIN_FOR_GEO_")) {
                    Long id = Long.parseLong(callData.substring(19));
                    stateMap.put(chatId, UserState.ADD_DOMAIN);
                    addDomainMap.put(chatId, id);
                    messageSender.addDomainMessage(chatId);
                }else if (callData.startsWith("ADD_PROXY_FOR_GEO_")) {
                    Long id = Long.parseLong(callData.substring(18));
                    stateMap.put(chatId, UserState.ADD_PROXY);
                    addProxyMap.put(chatId, id);
                    messageSender.addProxyMessage(chatId);
                } else if (callData.equals("RESET_PROXY")) {
                    resetMap(chatId);
                    messageSender.resetProxy(chatId);
                }

            }

        }
    }

    private void resetMap(Long chatId) {
        stateMap.remove(chatId);
        addDomainMap.remove(chatId);
        addProxyMap.remove(chatId);
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
