package com.example.proxytgbot.services;

import com.example.proxytgbot.models.entities.Key;
import com.example.proxytgbot.models.entities.User;
import com.example.proxytgbot.models.enums.KeyStatus;
import com.example.proxytgbot.models.enums.Role;
import com.example.proxytgbot.repositories.KeyRepo;
import com.example.proxytgbot.repositories.UserRepo;
import com.example.proxytgbot.services.interfaces.MessageSender;
import com.example.proxytgbot.services.interfaces.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SecurityServiceImpl implements SecurityService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private KeyRepo keyRepo;
    @Autowired
    private MessageSender messageSender;

    @Override
    public boolean hasKeyByChatId(Long chatId, boolean sendMessage) {
        if(userRepo.findUserByTelegramChatId(chatId).getKey() != null){
            return true;
        }else {
            if(sendMessage) {
                messageSender.sendMessage(chatId, "Для работы в боте укажите ключ /key");
            }
            return false;
        }
    }

    @Override
    public void saveNewUser(Long chatId) {
        if(!userRepo.existsByTelegramChatId(chatId)) {
            User user = new User();
            user.setRole(Role.USER);
            user.setTelegramChatId(chatId);
            user.setKey(null);
            userRepo.save(user);
        }
    }

    @Override
    public void connectKeyToUser(Long chatId, String msg) {
        String key = msg.substring(5);
        if(keyRepo.existsById(key) && (keyRepo.findById(key).get().getKeyStatus() == KeyStatus.FREE)){
            User user = userRepo.findUserByTelegramChatId(chatId);
            Key keyEntity = keyRepo.findById(key).get();
            user.setKey(keyEntity);
            keyEntity.setKeyStatus(KeyStatus.ACTIVE);
            userRepo.save(user);
            keyRepo.save(keyEntity);
            messageSender.sendMessage(chatId, "Успех! Теперь вы можете работать с ботом✅");
        }else messageSender.sendMessage(chatId, "Неправильный ключ авторизации❌");
    }
}
