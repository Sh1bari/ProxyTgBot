package com.example.proxytgbot.telegram;


import com.example.proxytgbot.config.BotConfig;
import com.example.proxytgbot.models.enums.UserState;
import com.example.proxytgbot.services.interfaces.MessageSender;
import com.example.proxytgbot.services.interfaces.SecurityService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
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

    @Override
    public void onUpdateReceived(Update update) {
        long chatId;
        try {
            chatId = update.getMessage().getChatId();
        } catch (Exception e) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }
        securityService.saveNewUser(chatId);

        if(update.hasMessage() &&
                update.getMessage().hasText() &&
                !securityService.hasKeyByChatId(chatId, false) &&
                update.getMessage().getText().startsWith("/key")){
            String messageText = update.getMessage().getText();
            securityService.connectKeyToUser(chatId, messageText);

        } else if(securityService.hasKeyByChatId(chatId, true)){

            if(update.hasMessage() && update.getMessage().hasText()){
                String messageText = update.getMessage().getText();
                if(messageText.equals("/menu")){
                    messageSender.showMainMenu(chatId);
                }

            }else if (update.hasCallbackQuery()){
                String callData = update.getCallbackQuery().getData();
                int messageId = update.getCallbackQuery().getMessage().getMessageId();
                String inlineMessageId = update.getCallbackQuery().getInlineMessageId();
                chatId = update.getCallbackQuery().getMessage().getChatId();
                if (callData.equals("SOLVED_PROBLEM")) {
                    messageSender.deleteMessage(chatId, messageId);
                }else if(callData.equals("DOMAIN_MENU")){
                    messageSender.showDomainMenu(chatId);
                }else if(callData.equals("PROXY_MENU")){
                    //messageSender.showProxyMenu(chatId);
                }else if(callData.equals("SHOW_DOMAINS")){
                    messageSender.showDomains(chatId);
                }else if(callData.equals("ADD_DOMAINS")){
                    //messageSender.showDomainMenu(chatId);
                }else if(callData.equals("SHOW_MENU")){
                    messageSender.showMainMenu(chatId);
                }else if(callData.equals("DELETE_DOMAIN")){
                    messageSender.deleteDomainButton(chatId);
                }else if(callData.startsWith("DELETE_DOMAIN_CONF_")){
                    String domain = "https://" + callData.substring(19);
                    messageSender.deleteDomainConfirmation(chatId, domain);
                }else if(callData.startsWith("DELETE_DOMAIN_ACCEPT_")){
                    String domain = callData.substring(21);
                    messageSender.deleteDomain(chatId, domain, messageId);
                }

            }

        }
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
