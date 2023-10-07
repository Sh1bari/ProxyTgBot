package com.example.proxytgbot.repositories;

import com.example.proxytgbot.models.entities.Proxy;
import org.springframework.data.repository.CrudRepository;

public interface ProxyRepo extends CrudRepository<Proxy, Long> {
}
