package com.example.proxytgbot.services;

import com.example.proxytgbot.config.BotConfig;
import com.example.proxytgbot.models.entities.*;
import com.example.proxytgbot.models.enums.DomainStatus;
import com.example.proxytgbot.models.enums.KeyStatus;
import com.example.proxytgbot.models.enums.ProxyStatus;
import com.example.proxytgbot.models.enums.Role;
import com.example.proxytgbot.repositories.*;
import com.example.proxytgbot.services.interfaces.MessageSender;
import com.example.proxytgbot.services.interfaces.SecurityService;
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
                createInlineKeyboardButton("Домены\uD83D\uDDA5", "DOMAIN_MENU", null),
                createInlineKeyboardButton("Прокси\uD83C\uDF10", "PROXY_MENU", null)
        ));
        markup.setKeyboard(rowsInline);

        sendMessageWithButtons(chatId, "С чем хотите работать?", markup);
    }

    @Override
    public void showMainMenuForAdmin(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Домены\uD83D\uDDA5", "DOMAIN_MENU", null),
                createInlineKeyboardButton("Прокси\uD83C\uDF10", "PROXY_MENU", null)
        ));
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Ключ доступа\uD83D\uDD11", "CREATE_KEY", null),
                createInlineKeyboardButton("ГЕО\uD83D\uDDFA", "SHOW_GEO", null)
        ));
        markup.setKeyboard(rowsInline);

        sendMessageWithButtons(chatId, "С чем хотите работать?", markup);
    }

    @Override
    public void showDomainMenu(Long chatId) {
        List<Domain> domainList = domainRepo.findAllByUser_TelegramChatIdAndStatus(chatId, DomainStatus.ACTIVE);
        List<Domain> domainBanList = domainRepo.findAllByUser_TelegramChatIdAndStatus(chatId, DomainStatus.BANNED);
        List<Geo> geoList = (List<Geo>) geoRepo.findAll();
        StringBuilder str = new StringBuilder();
        AtomicInteger iter = new AtomicInteger(1);
        str.append("Список доменов:\n");
        for (Geo geo : geoList) {
            str.append(geo.getName()).append(":\n");
            domainList.forEach(o -> {
                if (o.getGeo().getName().equals(geo.getName())) {
                    str.append(iter.get()).append(") ").append(o.getDomain()).append("\n");
                    iter.getAndIncrement();
                }
            });
            domainBanList.forEach(o -> {
                if (o.getGeo().getName().equals(geo.getName())) {
                    str.append(iter.get()).append(") ").append(o.getDomain()).append(" ❌").append("\n");
                    iter.getAndIncrement();
                }
            });

        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Добавить\uD83D\uDCDD", "ADD_DOMAINS", null),
                createInlineKeyboardButton("Удалить\uD83D\uDDD1️", "DELETE_DOMAIN", null)
        ));
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Назад↩", "SHOW_MENU", null)
        ));
        markup.setKeyboard(rowsInline);

        sendMessageWithButtons(chatId, str.toString(), markup);

    }

    @Override
    public void showProxyMenu(Long chatId) {
        List<Proxy> proxyList = proxyRepo.findAllByUser_TelegramChatIdAndStatus(chatId, ProxyStatus.ACTIVE);
        List<Proxy> proxyErrorList = proxyRepo.findAllByUser_TelegramChatIdAndStatus(chatId, ProxyStatus.ERROR);
        List<Geo> geoList = (List<Geo>) geoRepo.findAll();
        StringBuilder str = new StringBuilder();
        AtomicInteger iter = new AtomicInteger(1);
        str.append("Список прокси:\n");
        for (Geo geo : geoList) {
            str.append(geo.getName()).append(":\n");
            proxyList.forEach(o -> {
                if (o.getGeo().getName().equals(geo.getName())) {
                    str.append(iter.get()).append(") ").append(o.getCode()).append("\n");
                    iter.getAndIncrement();
                }
            });
            proxyErrorList.forEach(o -> {
                if (o.getGeo().getName().equals(geo.getName())) {
                    str.append(iter.get()).append(") ").append(o.getCode()).append("❓\n");
                    iter.getAndIncrement();
                }
            });
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Добавить\uD83D\uDCDD", "ADD_PROXY", null),
                createInlineKeyboardButton("Удалить\uD83D\uDDD1️", "DELETE_PROXY", null)
        ));
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Восстановить все прокси\uD83D\uDD04", "RESET_PROXY", null)
        ));
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Назад↩", "SHOW_MENU", null)
        ));
        markup.setKeyboard(rowsInline);

        sendMessageWithButtons(chatId, str.toString(), markup);
    }

    @Override
    public void deleteDomainButton(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<Domain> domainList = domainRepo.findAllByUser_TelegramChatIdAndStatus(chatId, DomainStatus.ACTIVE);
        domainList.forEach(o -> {
            String name = o.getDomain().substring(8);
            rowsInline.add(createInlineKeyboardButtonList(
                    createInlineKeyboardButton(name, "DELETE_DOMAIN_CONF_" + name, null)
            ));
        });
        List<Domain> domainBanList = domainRepo.findAllByUser_TelegramChatIdAndStatus(chatId, DomainStatus.BANNED);
        domainBanList.forEach(o -> {
            String name = o.getDomain().substring(8);
            rowsInline.add(createInlineKeyboardButtonList(
                    createInlineKeyboardButton(name + " ❌", "DELETE_DOMAIN_CONF_" + name, null)
            ));
        });
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Назад↩", "DOMAIN_MENU", null)
        ));
        markup.setKeyboard(rowsInline);

        sendMessageWithButtons(chatId, "Выберите домен для удаления:\n", markup);
    }

    @Override
    public void deleteGeoButton(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<Geo> geoList = (List<Geo>) geoRepo.findAll();
        geoList.forEach(o -> {
            rowsInline.add(createInlineKeyboardButtonList(
                    createInlineKeyboardButton(o.getName(), "DELETE_GEO_CONF_" + o.getId(), null)
            ));
        });
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Назад↩", "SHOW_GEO", null)
        ));
        markup.setKeyboard(rowsInline);

        sendMessageWithButtons(chatId, "Выберите ГЕО для удаления:\n", markup);
    }

    @Override
    public void deleteDomain(Long chatId, String domainStr, Integer messageId) {
        List<Domain> domain = domainRepo.findDomainsByUser_TelegramChatIdAndDomain(chatId, domainStr);
        domain.forEach(o->{
            domainRepo.deleteFun(o.getId());
        });
        deleteMessage(chatId, messageId);
        deleteDomainButton(chatId);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Меню\uD83D\uDCD6", "SHOW_MENU", null)
        ));
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Список доменов↩", "DOMAIN_MENU", null)
        ));
        markup.setKeyboard(rowsInline);
        sendMessageWithButtons(chatId, "Домен удален✅", markup);
    }

    @Override
    public void deleteProxy(Long chatId, Long id, Integer messageId) {
        proxyRepo.deleteFun(id);
        deleteMessage(chatId, messageId);
        deleteProxyButton(chatId);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Меню\uD83D\uDCD6", "SHOW_MENU", null)
        ));
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Список прокси↩", "PROXY_MENU", null)
        ));
        markup.setKeyboard(rowsInline);
        sendMessageWithButtons(chatId, "Прокси удален✅", markup);
    }

    @Override
    public void deleteGeo(Long chatId, Long id, Integer messageId) {
        geoRepo.deleteProxiesByGeoId(id);
        geoRepo.deleteDomainsByGeoId(id);
        geoRepo.deleteById(id);

        deleteMessage(chatId, messageId);
        deleteGeoButton(chatId);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Меню\uD83D\uDCD6", "SHOW_MENU", null)
        ));
        markup.setKeyboard(rowsInline);

        sendMessageWithButtons(chatId, "ГЕО удален✅", markup);
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

        sendMessageWithButtons(chatId, "Вы уверены, что хотите удалить домен:\n" + domain, markup);
    }

    @Override
    public void deleteProxyConfirmation(Long chatId, Long id) {
        Proxy proxy = proxyRepo.findById(id).get();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Да✅", "DELETE_PROXY_ACCEPT_" + id, null),
                createInlineKeyboardButton("Нет❌", "SOLVED_PROBLEM", null)
        ));
        markup.setKeyboard(rowsInline);

        sendMessageWithButtons(chatId, "Вы уверены, что хотите удалить прокси:\n" + proxy.getCode(), markup);
    }

    @Override
    public void deleteGeoConfirmation(Long chatId, Long id) {
        Geo geo = geoRepo.findById(id).get();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Да✅", "DELETE_GEO_ACCEPT_" + id, null),
                createInlineKeyboardButton("Нет❌", "SOLVED_PROBLEM", null)
        ));
        markup.setKeyboard(rowsInline);

        sendMessageWithButtons(chatId, "Вы уверены, что хотите удалить ГЕО:\n" + geo.getName(), markup);
    }

    @Override
    public void createKey(Long chatId) {
        Key key = new Key();
        key.setKeyStatus(KeyStatus.FREE);
        keyRepo.save(key);
        sendMessage(chatId, "Новый ключ доступа:\n");
        sendMessage(chatId, "/key " + key.getId());
    }

    @Override
    public void showGeo(Long chatId) {
        List<Geo> geoList = (List<Geo>) geoRepo.findAll();
        StringBuilder str = new StringBuilder();
        AtomicInteger iter = new AtomicInteger(1);
        str.append("Список ГЕО:\n");
        for (Geo geo : geoList) {
            str.append(iter.get()).append(") ").append(geo.getName()).append("\n");
            iter.getAndIncrement();
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Добавить\uD83D\uDCDD", "ADD_GEO", null),
                createInlineKeyboardButton("Удалить\uD83D\uDDD1️", "DELETE_GEO", null)
        ));
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Назад↩", "SHOW_MENU", null)
        ));
        markup.setKeyboard(rowsInline);

        sendMessageWithButtons(chatId, str.toString(), markup);
    }

    @Override
    public void deleteProxyButton(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<Proxy> proxyList = proxyRepo.findAllByUser_TelegramChatIdAndStatusOrderByCodeAsc(chatId, ProxyStatus.ACTIVE);
        proxyList.forEach(o -> {
            List<String> name = Arrays.stream(o.getCode().split(":")).toList();
            rowsInline.add(createInlineKeyboardButtonList(
                    createInlineKeyboardButton(name.get(1) + "\n" + name.get(3), "DELETE_PROXY_CONF_" + o.getId(), null)
            ));
        });
        List<Proxy> proxyErrorList = proxyRepo.findAllByUser_TelegramChatIdAndStatusOrderByCodeAsc(chatId, ProxyStatus.ERROR);
        proxyErrorList.forEach(o -> {
            List<String> name = Arrays.stream(o.getCode().split(":")).toList();
            rowsInline.add(createInlineKeyboardButtonList(
                    createInlineKeyboardButton(name.get(1) + "\n" + name.get(3) + "❓", "DELETE_PROXY_CONF_" + o.getId(), null)
            ));
        });
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Назад↩", "PROXY_MENU", null)
        ));
        markup.setKeyboard(rowsInline);

        sendMessageWithButtons(chatId, "Выберите прокси для удаления:\n", markup);
    }

    @Override
    public void resetProxy(Long chatId) {
        List<Proxy> proxyList = proxyRepo.findAllByUser_TelegramChatIdAndStatus(chatId, ProxyStatus.ERROR);
        proxyList.forEach(o->{
            o.setStatus(ProxyStatus.ACTIVE);
            proxyRepo.save(o);
        });
        showProxyMenu(chatId);
        sendMessage(chatId, "Все прокси восстановлены✅");
    }

    @Override
    public void addGeoMessage(Long chatId) {
        sendMessage(chatId, "Введите название:");
    }

    @Override
    public void addDomainMessage(Long chatId) {
        sendMessage(chatId, "Введите домен:");
    }

    @Override
    public void addProxyMessage(Long chatId) {
        sendMessage(chatId, "Введите прокси:");
    }

    @Override
    public boolean addGeo(Long chatId, String geo) {
        if (geoRepo.existsByName(geo)) {
            sendMessage(chatId, "Такая ГЕО уже есть↩");
            return false;
        } else {
            Geo geoEnt = new Geo();
            geoEnt.setName(geo);
            geoRepo.save(geoEnt);

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            rowsInline.add(createInlineKeyboardButtonList(
                    createInlineKeyboardButton("Меню\uD83D\uDCD6", "SHOW_MENU", null)
            ));
            markup.setKeyboard(rowsInline);
            sendMessageWithButtons(chatId, "Гео " + geo + " успешно добавлена✅", markup);
            return true;
        }
    }

    @Override
    public boolean addDomain(Long chatId, String domain1, Long geoId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Меню\uD83D\uDCD6", "SHOW_MENU", null)
        ));
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Список доменов↩", "DOMAIN_MENU", null)
        ));
        markup.setKeyboard(rowsInline);
        StringBuilder str = new StringBuilder();
        String[] domains = domain1.split("\n");
        for (String domain : domains) {
            domain = domain.replace(" ", "");
            if (domain.startsWith("http:// ")) {
                domain = "https://" + domain.substring(8);
            } else if (domain.startsWith("http://")) {
                domain = "https://" + domain.substring(7);
            } else if (domain.startsWith("https:// ")) {
                domain = "https://" + domain.substring(9);
            } else if (domain.startsWith("https://")) {

            } else domain = "https://" + domain;

            if (domainRepo.existsByDomainAndGeo_IdAndStatus(domain, geoId, DomainStatus.ACTIVE)) {
                sendMessage(chatId, "Такой домен уже есть↩\n " +
                        domain);
            } else {

                User user = userRepo.findUserByTelegramChatId(chatId);

                Geo geo = geoRepo.findById(geoId).get();

                Domain domainEnt = new Domain();
                domainEnt.setDomain(domain);
                domainEnt.setStatus(DomainStatus.ACTIVE);
                domainEnt.setGeo(geo);
                domainEnt.setUser(user);
                user.getDomains().add(domainEnt);
                geo.getDomains().add(domainEnt);
                userRepo.save(user);
                str.append(domain).append("\n");
            }
        }
        if(!str.isEmpty()) {
            sendMessageWithButtons(chatId, "Домен:\n" + str + " успешно добавлен(ы)✅", markup);
            return true;
        }else return false;
    }

    @Override
    public boolean addProxy(Long chatId, String proxy, Long geoId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Меню\uD83D\uDCD6", "SHOW_MENU", null)
        ));
        rowsInline.add(createInlineKeyboardButtonList(
                createInlineKeyboardButton("Прокси↩", "PROXY_MENU", null)
        ));
        markup.setKeyboard(rowsInline);
        StringBuilder str = new StringBuilder();
        String[] proxyList = proxy.split("\n");
        for (String s : proxyList) {
            s = s.replace(" ", "");
            List<String> proxyInfo = List.of(proxy.split(":"));
            try {
                String proxyHost = proxyInfo.get(0);
                int proxyPort = Integer.parseInt(proxyInfo.get(1));
                String proxyUsername = proxyInfo.get(2);
                String proxyPassword = proxyInfo.get(3);
            } catch (Exception e) {
                sendMessage(chatId, "Неправильный формат ввода прокси❌\n" +
                        s);
            }
            if (proxyRepo.existsByCodeAndGeo_IdAndStatus(s, geoId, ProxyStatus.ACTIVE)) {
                sendMessage(chatId, "Такой прокси уже есть↩\n" +
                        s);
            } else {

                User user = userRepo.findUserByTelegramChatId(chatId);

                Geo geo = geoRepo.findById(geoId).get();

                Proxy proxyEnt = new Proxy();
                proxyEnt.setStatus(ProxyStatus.ACTIVE);
                proxyEnt.setUser(user);
                proxyEnt.setGeo(geo);
                proxyEnt.setCode(s);
                user.getProxies().add(proxyEnt);
                geo.getProxies().add(proxyEnt);
                userRepo.save(user);
                str.append(s).append("\n");
            }

        }
        if(!str.isEmpty()) {
            sendMessageWithButtons(chatId, "Прокси:\n " + str + " \nуспешно добавлен(ы)✅", markup);
            return true;
        }else return false;
    }

    @Override
    public void chooseGeoForDomain(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<Geo> geoList = (List<Geo>) geoRepo.findAll();
        geoList.forEach(o -> {
            rowsInline.add(createInlineKeyboardButtonList(
                    createInlineKeyboardButton(o.getName(), "ADD_DOMAIN_FOR_GEO_" + o.getId(), null)
            ));
        });
        markup.setKeyboard(rowsInline);
        sendMessageWithButtons(chatId, "Выберите для какого ГЕО:\n", markup);
    }

    @Override
    public void chooseGeoForProxy(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<Geo> geoList = (List<Geo>) geoRepo.findAll();
        geoList.forEach(o -> {
            rowsInline.add(createInlineKeyboardButtonList(
                    createInlineKeyboardButton(o.getName(), "ADD_PROXY_FOR_GEO_" + o.getId(), null)
            ));
        });
        markup.setKeyboard(rowsInline);
        sendMessageWithButtons(chatId, "Выберите для какого ГЕО:\n", markup);
    }

    @Override
    public void makeAdmin(Long chatId) {
        User user = userRepo.findUserByTelegramChatId(chatId);
        user.setRole(Role.ADMIN);
        userRepo.save(user);
        sendMessage(chatId, "Теперь вы админ✅");
    }
    @Override
    public void makeAdminKey(Long chatId) {
        Key key1 = new Key();
        key1.setKeyStatus(KeyStatus.FREE);
        keyRepo.save(key1);
        String key = key1.getId();
        if (keyRepo.existsById(key) && (keyRepo.findById(key).get().getKeyStatus() == KeyStatus.FREE)) {
            User user = userRepo.findUserByTelegramChatId(chatId);
            Key keyEntity = keyRepo.findById(key).get();
            user.setKey(keyEntity);
            keyEntity.setKeyStatus(KeyStatus.ACTIVE);
            userRepo.save(user);
            keyRepo.save(keyEntity);
            sendMessage(chatId, "Успех! Теперь вы можете работать с ботом✅");
        } else sendMessage(chatId, "Неправильный ключ авторизации❌");
    }

    private List<InlineKeyboardButton> createInlineKeyboardButtonList(InlineKeyboardButton... buttons) {
        return new ArrayList<>(Arrays.asList(buttons));
    }

    private InlineKeyboardButton createInlineKeyboardButton(String text, String callbackData, String url) {
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
    @Autowired
    private KeyRepo keyRepo;
    @Autowired
    private ProxyRepo proxyRepo;
    @Autowired
    private UserRepo userRepo;

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
