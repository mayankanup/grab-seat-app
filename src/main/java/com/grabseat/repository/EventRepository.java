package com.grabseat.repository;

import com.grabseat.model.Event;
import com.grabseat.model.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByType(EventType type);

    @Query("SELECT e FROM Event e WHERE "
        + "(:keyword IS NULL OR :keyword = '' "
        + " OR LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
        + " OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
        + "AND (:start IS NULL OR e.startTime >= :start) "
        + "AND (:end IS NULL OR e.startTime <= :end)")
    Page<Event> search(@Param("keyword") String keyword,
                       @Param("start") LocalDateTime start,
                       @Param("end") LocalDateTime end,
                       Pageable pageable);
}
