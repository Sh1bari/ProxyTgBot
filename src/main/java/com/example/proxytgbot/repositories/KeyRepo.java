package com.example.proxytgbot.repositories;

import com.example.proxytgbot.models.entities.Key;
import org.springframework.data.repository.CrudRepository;

public interface KeyRepo extends CrudRepository<Key, String> {
    boolean existsById(String id);
}
