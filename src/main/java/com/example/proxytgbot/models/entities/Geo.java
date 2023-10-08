package com.example.proxytgbot.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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

    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "geo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Proxy> proxies;

    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "geo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Domain> domains;

}
