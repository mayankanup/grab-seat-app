package com.grabseat.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Debezium CDC consumer: Postgres WAL -> Debezium Server -> Kafka topic
 * {@code dbserver1.public.events} -> Elasticsearch. Upserts are idempotent
 * (document id = event id), deletes drop the doc. Every failure is caught
 * and logged so a poison message never blocks the partition.
 */
@Component
public class EventCdcConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventCdcConsumer.class);

    static final String TOPIC = "dbserver1.public.events";

    private final ObjectMapper mapper;
    private final EventSearchIndexer indexer;

    public EventCdcConsumer(ObjectMapper mapper, EventSearchIndexer indexer) {
        this.mapper = mapper;
        this.indexer = indexer;
    }

    @KafkaListener(topics = TOPIC, groupId = "grabseat-search-indexer")
    public void onMessage(String key, String value) {
        try {
            handle(value);
        } catch (Exception e) {
            log.warn("Skipping unprocessable CDC message (key={}): {}", key, e.toString());
        }
    }

    void handle(String value) throws Exception {
        JsonNode root = mapper.readTree(value);
        JsonNode envelope = root.has("payload") && root.get("payload").isObject()
            ? root.get("payload")
            : root;
        String op = envelope.path("op").asText("");
        if ("d".equals(op)) {
            long id = envelope.path("before").path("id").asLong(-1);
            if (id > 0) {
                indexer.deleteEvent(id);
            }
            return;
        }
        long id = envelope.path("after").path("id").asLong(-1);
        if (id > 0) {
            indexer.indexEventById(id);
        }
    }
}
