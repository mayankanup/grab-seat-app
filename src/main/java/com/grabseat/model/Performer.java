package com.grabseat.model;

import jakarta.persistence.*;

@Entity
@Table(name = "performers")
public class Performer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String type;

    protected Performer() {
    }

    public Performer(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
}
