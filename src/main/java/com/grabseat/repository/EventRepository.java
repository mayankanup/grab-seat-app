package com.grabseat.repository;

import com.grabseat.model.Event;
import com.grabseat.model.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByType(EventType type);
}
