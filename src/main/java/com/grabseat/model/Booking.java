package com.grabseat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.RESERVED;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Booking() {
    }

    public Booking(String userId, Ticket ticket, BookingStatus status) {
        this.userId = userId;
        this.ticket = ticket;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public Ticket getTicket() { return ticket; }
    public BookingStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
    }
}
