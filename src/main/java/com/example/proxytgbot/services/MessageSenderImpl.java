package com.example.proxytgbot.services;

import com.example.proxytgbot.config.BotConfig;
import com.example.proxytgbot.models.entities.Domain;
import com.example.proxytgbot.models.entities.Geo;
import com.example.proxytgbot.models.enums.DomainStatus;
import com.example.proxytgbot.repositories.DomainRepo;
import com.example.proxytgbot.repositories.GeoRepo;
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
import java.util.concurrent.atomic.AtomicInteger;

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

    @Override
    public void showMainMenu(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Домены", "DOMAIN_MENU", null),
                createInlineKeyboardButton("Прокси", "PROXY_MENU", null)
                ));
        markup.setKeyboard(rowsInline);

        sendMessageWithButtons(chatId, "С чем хотите работать?", markup);
    }

    @Override
    public void showDomainMenu(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Показать", "SHOW_DOMAINS", null),
                createInlineKeyboardButton("Добавить", "ADD_DOMAINS", null)
        ));
        markup.setKeyboard(rowsInline);

        sendMessageWithButtons(chatId, "Меню доменов", markup);
    }

    @Override
    public void showProxyMenu(Long chatId) {

    }

    @Override
    public void showDomains(Long chatId) {
        List<Domain> domainList = domainRepo.findAllByUser_TelegramChatIdAndStatus(chatId, DomainStatus.ACTIVE);
        List<Geo> geoList = (List<Geo>) geoRepo.findAll();
        StringBuilder str = new StringBuilder();
        AtomicInteger iter = new AtomicInteger(1);
        str.append("Список доменов:\n");
        for(Geo geo : geoList){
            str.append(geo.getName()).append(":\n");
            domainList.forEach(o->{
                if(o.getGeo().getName().equals(geo.getName())){
                    str.append(iter.get()).append(") ").append(o.getDomain()).append("\n");
                    iter.getAndIncrement();
                }
            });
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Назад↩", "DOMAIN_MENU", null),
                createInlineKeyboardButton("Удалить\uD83D\uDDD1️", "DELETE_DOMAIN", null)
        ));
        markup.setKeyboard(rowsInline);

        sendMessageWithButtons(chatId, str.toString(), markup);
    }

    @Override
    public void deleteDomainButton(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<Domain> domainList = domainRepo.findAllByUser_TelegramChatIdAndStatus(chatId, DomainStatus.ACTIVE);
        domainList.forEach(o->{
            String name = o.getDomain().substring(8);
                    rowsInline.add(createInlineKeyboardButtonList(
                    createInlineKeyboardButton(name, "DELETE_DOMAIN_CONF_" + name, null)
            ));
        });
        markup.setKeyboard(rowsInline);

        sendMessageWithButtons(chatId, "Выберите домен для удаления:\n", markup);
    }

    @Override
    public void deleteDomain(Long chatId, String domainStr, Integer messageId) {
        Domain domain = domainRepo.findDomainByUser_TelegramChatIdAndDomain(chatId, domainStr);
        domain.setStatus(DomainStatus.BANNED);
        domainRepo.save(domain);
        deleteMessage(chatId, messageId);
        deleteDomainButton(chatId);
        sendMessage(chatId, "Домен удален✅");
    }

    @Override
    public void deleteDomainConfirmation(Long chatId, String domain) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Да✅", "DELETE_DOMAIN_ACCEPT_" + domain, null),
                createInlineKeyboardButton("Нет❌", "SOLVED_PROBLEM", null)
        ));
        markup.setKeyboard(rowsInline);

        sendMessageWithButtons(chatId,"Вы уверены, что хотите удалить домен:\n" + domain, markup);
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
    @Autowired
    private DomainRepo domainRepo;
    @Autowired
    private GeoRepo geoRepo;

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
