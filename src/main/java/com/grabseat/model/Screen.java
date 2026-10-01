package com.grabseat.model;

import jakarta.persistence.*;

@Entity
@Table(name = "screens")
public class Screen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    protected Screen() {
    }

    public Screen(Venue venue, String name, Integer capacity) {
        this.venue = venue;
        this.name = name;
        this.capacity = capacity;
    }

    public Long getId() { return id; }
    public Venue getVenue() { return venue; }
    public String getName() { return name; }
    public Integer getCapacity() { return capacity; }

    public void update(String name, Integer capacity) {
        this.name = name;
        this.capacity = capacity;
    }
}
