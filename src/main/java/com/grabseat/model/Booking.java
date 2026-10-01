package com.grabseat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @OneToMany(mappedBy = "booking")
    private List<Ticket> tickets = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.RESERVED;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Booking() {
    }

    public Booking(String userId, List<Ticket> tickets) {
        this.userId = userId;
        this.status = BookingStatus.RESERVED;
        this.tickets = new ArrayList<>(tickets);
        for (Ticket t : this.tickets) {
            t.assignBooking(this);
        }
    }

    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public List<Ticket> getTickets() { return tickets; }
    public BookingStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
    }
}
