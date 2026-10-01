package com.grabseat.search;

import com.grabseat.model.Event;
import com.grabseat.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the Elasticsearch index fed: backfills once on startup (after the
 * seeder) and exposes indexEvent() for write paths. All ES contact goes
 * through the connection-lazy template inside try/catch, so the app boots
 * and serves (via DB fallback) with no Elasticsearch around.
 * (Debezium CDC + Kafka will replace dual-write later per FinalArchitecture.)
 */
@Component
public class EventSearchIndexer {

    private static final Logger log = LoggerFactory.getLogger(EventSearchIndexer.class);

    private final EventRepository events;
    private final ElasticsearchOperations operations;

    public EventSearchIndexer(EventRepository events, ElasticsearchOperations operations) {
        this.events = events;
        this.operations = operations;
    }

    public void indexEvent(Event event) {
        try {
            ensureIndex();
            operations.save(toDocument(event));
        } catch (Exception e) {
            log.warn("Search index write failed for event {}: {}", event.getId(), e.toString());
        }
    }

    public boolean indexHasData() {
        try {
            return operations.count(new CriteriaQuery(new Criteria()),
                EventSearchDocument.class) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public EventSearchDocument toDocument(Event event) {
        return new EventSearchDocument(
            event.getId(), event.getName(), event.getDescription(),
            event.getType() != null ? event.getType().name() : null,
            event.getStartTime(), event.getEndTime(),
            event.getBasePrice() != null ? event.getBasePrice().doubleValue() : null,
            event.getVenue() != null ? event.getVenue().getName() : null,
            event.getPerformer() != null ? event.getPerformer().getName() : null,
            event.getScreen() != null ? event.getScreen().getName() : null);
    }

    private void ensureIndex() {
        var indexOps = operations.indexOps(EventSearchDocument.class);
        if (!indexOps.exists()) {
            indexOps.createWithMapping();
        }
    }

    @Transactional(readOnly = true)
    public void backfill() {
        if (!indexHasData() && events.count() > 0) {
            List<Event> all = new ArrayList<>();
            events.findAll().forEach(all::add);
            for (Event e : all) {
                indexEvent(e);
            }
            log.info("Search index backfilled with {} events", all.size());
        }
    }

    @Component
    @Order(Ordered.LOWEST_PRECEDENCE)
    public static class Backfill implements CommandLineRunner {

        private final EventSearchIndexer indexer;

        public Backfill(EventSearchIndexer indexer) {
            this.indexer = indexer;
        }

        @Override
        public void run(String... args) {
            try {
                indexer.backfill();
            } catch (Exception e) {
                log.warn("Search index backfill skipped: {}", e.toString());
            }
        }
    }
}
