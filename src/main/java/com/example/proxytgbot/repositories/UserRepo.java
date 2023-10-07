package com.example.proxytgbot.repositories;

import com.example.proxytgbot.models.entities.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepo extends CrudRepository<User, Long> {
    User findUserByTelegramChatId(Long chatId);

    boolean existsByTelegramChatId(Long chatId);
}
