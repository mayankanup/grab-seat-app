package com.grabseat.seed;

import com.grabseat.model.*;
import com.grabseat.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * US0: Seed DB with a few movies and comedy shows so view/search/book can be demoed.
 * Runs on startup, skips if events already exist. Idempotent.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final VenueRepository venues;
    private final PerformerRepository performers;
    private final EventRepository events;
    private final TicketRepository tickets;
    private final ScreenRepository screens;
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String adminLogin;
    private final String adminPassword;

    public DataSeeder(VenueRepository venues, PerformerRepository performers,
                      EventRepository events, TicketRepository tickets,
                      ScreenRepository screens, UserAccountRepository users,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.admin.login:admin}") String adminLogin,
                      @Value("${app.admin.password:admin123}") String adminPassword) {
        this.venues = venues;
        this.performers = performers;
        this.events = events;
        this.tickets = tickets;
        this.screens = screens;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.adminLogin = adminLogin;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        ensureAdmin();
        if (events.count() == 0) {
            seedEvents();
        }
        ensureScreens();
    }

    private void ensureScreens() {
        for (Venue venue : venues.findAll()) {
            if (screens.findByVenueIdOrderById(venue.getId()).isEmpty()) {
                screens.save(new Screen(venue, "Screen 1", venue.getCapacity()));
            }
        }
    }

    private void seedEvents() {
        Venue cinema = venues.save(new Venue("PVR Downtown Cinema", "Downtown Mall, Screen 4", 120));
        Venue comedyHall = venues.save(new Venue("Laugh Factory Hall", "MG Road, Auditorium B", 200));

        Performer duneCast = performers.save(new Performer("Dune: Part Two Cast", "MOVIE_CAST"));
        Performer interstellarCast = performers.save(new Performer("Interstellar Re-release Cast", "MOVIE_CAST"));
        Performer spiderCast = performers.save(new Performer("Spider-Verse Live Cast", "MOVIE_CAST"));
        Performer zakir = performers.save(new Performer("Zakir Khan", "COMEDIAN"));
        Performer biswa = performers.save(new Performer("Biswa Kalyan Rath", "COMEDIAN"));
        Performer anubhav = performers.save(new Performer("Anubhav Singh Bassi", "COMEDIAN"));

        LocalDateTime now = LocalDateTime.now().withNano(0);

        List<Event> seedEvents = List.of(
            new Event("Dune: Part Two - Evening Show",
                "Sci-fi epic on IMAX. Dolby 7.1 screening.",
                EventType.MOVIE, cinema, duneCast,
                now.plusDays(1).withHour(18).withMinute(0),
                now.plusDays(1).withHour(21).withMinute(0),
                new BigDecimal("349.00")),
            new Event("Interstellar Re-release - Weekend Special",
                "Christopher Nolan classic back on big screen.",
                EventType.MOVIE, cinema, interstellarCast,
                now.plusDays(2).withHour(15).withMinute(30),
                now.plusDays(2).withHour(18).withMinute(30),
                new BigDecimal("299.00")),
            new Event("Spider-Man: Across the Spider-Verse - Premiere",
                "Animated premiere with live dubbing artists Q&A.",
                EventType.MOVIE, cinema, spiderCast,
                now.plusDays(3).withHour(19).withMinute(0),
                now.plusDays(3).withHour(21).withMinute(30),
                new BigDecimal("399.00")),
            new Event("Zakir Khan Live - Tathastu Tour",
                "Stand-up special, 90 mins + 15 min opener.",
                EventType.COMEDY, comedyHall, zakir,
                now.plusDays(4).withHour(20).withMinute(0),
                now.plusDays(4).withHour(22).withMinute(0),
                new BigDecimal("999.00")),
            new Event("Biswa Kalyan Rath - Mood Kharaab",
                "Brand new hour + crowd work.",
                EventType.COMEDY, comedyHall, biswa,
                now.plusDays(5).withHour(19).withMinute(30),
                now.plusDays(5).withHour(21).withMinute(0),
                new BigDecimal("799.00")),
            new Event("Anubhav Singh Bassi - Kisi Ko Batana Mat",
                "Storytelling comedy, family friendly.",
                EventType.COMEDY, comedyHall, anubhav,
                now.plusDays(6).withHour(18).withMinute(0),
                now.plusDays(6).withHour(20).withMinute(0),
                new BigDecimal("899.00"))
        );

        for (Event e : seedEvents) {
            Event saved = events.save(e);
            tickets.saveAll(buildTickets(saved, 40));
        }
    }

    private void ensureAdmin() {
        if (users.findByLogin(adminLogin).isEmpty()) {
            users.save(new UserAccount(adminLogin, passwordEncoder.encode(adminPassword),
                "Administrator", "admin@grabseat.local", Role.ADMIN));
        }
    }

    private List<Ticket> buildTickets(Event event, int count) {
        List<Ticket> list = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            String seat = "A-" + i;
            list.add(new Ticket(event, seat, event.getBasePrice(), TicketStatus.AVAILABLE));
        }
        return list;
    }
}
