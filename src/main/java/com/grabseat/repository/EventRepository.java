package com.grabseat.repository;

import com.grabseat.model.Event;
import com.grabseat.model.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    List<Event> findByType(EventType type);
}
