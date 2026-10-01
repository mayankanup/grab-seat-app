package com.grabseat.service;

import com.grabseat.dto.CreateEventRequest;
import com.grabseat.model.*;
import com.grabseat.repository.PerformerRepository;
import com.grabseat.repository.ScreenRepository;
import com.grabseat.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Real-Postgres overlap checks: same screen clashes, other screens and
 * back-to-back slots are fine. Needs a database (like contextLoads).
 */
@SpringBootTest
@Transactional
class AdminSchedulingIntegrationTest {

    @Autowired
    AdminService admins;
    @Autowired
    VenueRepository venues;
    @Autowired
    ScreenRepository screens;
    @Autowired
    PerformerRepository performers;

    private Venue venue;
    private Screen s1;
    private Screen s2;
    private Performer performer;

    @BeforeEach
    void setup() {
        venue = venues.save(new Venue("Test Multiplex", "Test Town", 200));
        s1 = screens.save(new Screen(venue, "Screen 1", 100));
        s2 = screens.save(new Screen(venue, "Screen 2", 100));
        performer = performers.save(new Performer("Test Comic", "COMEDIAN"));
        admins.createEvent(base("Base Show", s1.getId(),
            LocalDateTime.of(2026, 12, 1, 10, 0), LocalDateTime.of(2026, 12, 1, 12, 0)));
    }

    private CreateEventRequest base(String name, Long screenId, LocalDateTime start,
                                    LocalDateTime end) {
        return new CreateEventRequest(name, null, EventType.COMEDY, venue.getId(),
            performer.getId(), screenId, start, end, new BigDecimal("100.00"), 2);
    }

    @Test
    void sameScreenOverlapIsRejected() {
        assertThatThrownBy(() -> admins.createEvent(base("Clash", s1.getId(),
            LocalDateTime.of(2026, 12, 1, 11, 0), LocalDateTime.of(2026, 12, 1, 13, 0))))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already has a show");
    }

    @Test
    void otherScreenAtSameTimeIsAllowed() {
        var created = admins.createEvent(base("Parallel", s2.getId(),
            LocalDateTime.of(2026, 12, 1, 11, 0), LocalDateTime.of(2026, 12, 1, 13, 0)));

        assertThat(created.screen().name()).isEqualTo("Screen 2");
    }

    @Test
    void backToBackOnSameScreenIsAllowed() {
        var created = admins.createEvent(base("Next", s1.getId(),
            LocalDateTime.of(2026, 12, 1, 12, 0), LocalDateTime.of(2026, 12, 1, 14, 0)));

        assertThat(created.tickets()).hasSize(2);
    }
}
