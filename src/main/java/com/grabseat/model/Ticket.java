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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Version
    private Long version;

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
    public Booking getBooking() { return booking; }

    void assignBooking(Booking booking) {
        this.booking = booking;
    }

    public void reserve(String userId) {
        if (status != TicketStatus.AVAILABLE) {
            throw new com.grabseat.exception.TicketNotAvailableException(
                "Ticket " + id + " is not available (status=" + status + ")");
        }
        this.status = TicketStatus.RESERVED;
        this.userId = userId;
    }

    public void confirm(String userId) {
        if (status != TicketStatus.RESERVED || !userId.equals(this.userId)) {
            throw new com.grabseat.exception.TicketNotAvailableException(
                "Ticket " + id + " cannot be confirmed by user " + userId);
        }
        this.status = TicketStatus.BOOKED;
    }
}
