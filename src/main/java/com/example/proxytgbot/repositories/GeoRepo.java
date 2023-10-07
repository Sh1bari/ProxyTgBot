package com.example.proxytgbot.repositories;

import com.example.proxytgbot.models.entities.Geo;
import org.springframework.data.repository.CrudRepository;

public interface GeoRepo extends CrudRepository<Geo, Long> {
}
