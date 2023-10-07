package com.example.proxytgbot.telegram;


import com.example.proxytgbot.config.BotConfig;
import com.example.proxytgbot.services.interfaces.MessageSender;
import com.example.proxytgbot.services.interfaces.SecurityService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@AllArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private BotConfig botConfig;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private MessageSender messageSender;

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

            }else if (update.hasCallbackQuery()){
                String callData = update.getCallbackQuery().getData();
                int messageId = update.getCallbackQuery().getMessage().getMessageId();
                String inlineMessageId = update.getCallbackQuery().getInlineMessageId();
                chatId = update.getCallbackQuery().getMessage().getChatId();
                if (callData.equals("SOLVED_PROBLEM")) {
                    messageSender.deleteMessage(chatId, messageId);
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
