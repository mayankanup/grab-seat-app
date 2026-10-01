package com.grabseat.repository;

import com.grabseat.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByTicketId(Long ticketId);
    List<Booking> findByUserId(String userId);
}
