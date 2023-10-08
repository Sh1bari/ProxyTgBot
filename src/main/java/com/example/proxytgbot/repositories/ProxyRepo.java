package com.example.proxytgbot.repositories;

import com.example.proxytgbot.models.entities.Proxy;
import com.example.proxytgbot.models.enums.DomainStatus;
import com.example.proxytgbot.models.enums.ProxyStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProxyRepo extends CrudRepository<Proxy, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM Proxy e WHERE e.id = :id")
    void deleteFun(@Param("id") Long id);

    List<Proxy> findAllByUser_TelegramChatIdAndStatus(Long chatId, ProxyStatus status);

    List<Proxy> findAllByUser_TelegramChatIdAndStatusOrderByCodeAsc(Long chatId, ProxyStatus status);
    boolean existsByCodeAndGeo_IdAndStatus(String code, Long geoId, ProxyStatus status);
}
