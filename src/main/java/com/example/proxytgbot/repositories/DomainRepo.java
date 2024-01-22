package com.example.proxytgbot.repositories;

import com.example.proxytgbot.models.entities.Domain;
import com.example.proxytgbot.models.enums.DomainStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DomainRepo extends CrudRepository<Domain, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM Domain e WHERE e.id = :id")
    void deleteFun(@Param("id") Long id);

    List<Domain> findAllByUser_TelegramChatIdAndStatus(Long chatId, DomainStatus status);

    List<Domain> findDomainsByUser_TelegramChatIdAndDomain(Long chatId, String domain);

    boolean existsByDomainAndGeo_IdAndStatus(String domain, Long geoId, DomainStatus status);
}
