package com.grabseat.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(nullable = false)
    private String seatNumber;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.AVAILABLE;

    private String userId;

    protected Ticket() {
    }

    public Ticket(Event event, String seatNumber, BigDecimal price, TicketStatus status) {
        this.event = event;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = status;
    }

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public String getSeatNumber() { return seatNumber; }
    public BigDecimal getPrice() { return price; }
    public TicketStatus getStatus() { return status; }
    public String getUserId() { return userId; }
}
