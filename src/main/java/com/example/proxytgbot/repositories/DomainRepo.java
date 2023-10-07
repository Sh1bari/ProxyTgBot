package com.example.proxytgbot.repositories;

import com.example.proxytgbot.models.entities.Domain;
import com.example.proxytgbot.models.entities.Key;
import org.springframework.data.repository.CrudRepository;

public interface DomainRepo extends CrudRepository<Domain, Long> {
}
