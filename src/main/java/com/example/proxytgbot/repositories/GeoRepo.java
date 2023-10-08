package com.example.proxytgbot.repositories;

import com.example.proxytgbot.models.entities.Geo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface GeoRepo extends CrudRepository<Geo, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM Proxy p WHERE p.geo.id = :id")
    void deleteProxiesByGeoId(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("DELETE FROM Domain d WHERE d.geo.id = :id")
    void deleteDomainsByGeoId(@Param("id") Long id);

    @Override
    @Modifying
    @Transactional
    @Query("DELETE FROM Geo g WHERE g.id = :id")
    void deleteById(@Param("id") Long id);

    boolean existsByName(String name);
}
