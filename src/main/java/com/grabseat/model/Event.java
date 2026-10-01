package com.grabseat.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performer_id")
    private Performer performer;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Column(nullable = false)
    private BigDecimal basePrice;

    @Column
    private String seriesId;

    protected Event() {
    }

    public Event(String name, String description, EventType type, Venue venue,
                 Performer performer, LocalDateTime startTime, LocalDateTime endTime,
                 BigDecimal basePrice) {
        this(name, description, type, venue, performer, startTime, endTime, basePrice, null);
    }

    public Event(String name, String description, EventType type, Venue venue,
                 Performer performer, LocalDateTime startTime, LocalDateTime endTime,
                 BigDecimal basePrice, String seriesId) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.venue = venue;
        this.performer = performer;
        this.startTime = startTime;
        this.endTime = endTime;
        this.basePrice = basePrice;
        this.seriesId = seriesId;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public EventType getType() { return type; }
    public Venue getVenue() { return venue; }
    public Performer getPerformer() { return performer; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public BigDecimal getBasePrice() { return basePrice; }
    public String getSeriesId() { return seriesId; }

    public void update(String name, String description, EventType type, Venue venue,
                       Performer performer, LocalDateTime startTime, LocalDateTime endTime,
                       BigDecimal basePrice) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.venue = venue;
        this.performer = performer;
        this.startTime = startTime;
        this.endTime = endTime;
        this.basePrice = basePrice;
    }
}
