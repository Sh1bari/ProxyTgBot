package com.example.proxytgbot.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "geos")
public class Geo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "geo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Proxy> proxies;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "geo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Domain> domains;

}
