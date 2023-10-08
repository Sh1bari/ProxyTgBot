package com.example.proxytgbot.repositories;

import com.example.proxytgbot.models.entities.Domain;
import com.example.proxytgbot.models.entities.Key;
import com.example.proxytgbot.models.enums.DomainStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DomainRepo extends CrudRepository<Domain, Long> {
    List<Domain> findAllByUser_TelegramChatIdAndStatus(Long chatId, DomainStatus status);
    Domain findDomainByUser_TelegramChatIdAndDomain(Long chatId, String domain);
}
