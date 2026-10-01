package com.grabseat.search;

import com.grabseat.model.*;
import com.grabseat.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventSearchIndexerTest {

    @Mock
    EventRepository events;
    @Mock
    ElasticsearchOperations operations;
    @Mock
    IndexOperations indexOps;

    @InjectMocks
    EventSearchIndexer indexer;

    private Event event() {
        Venue venue = new Venue("PVR", "Mall", 120);
        return new Event("Dune", "IMAX", EventType.MOVIE, venue, null,
            LocalDateTime.now().plusDays(1), null, new BigDecimal("349.00"));
    }

    @Test
    void indexEventSavesDocument() {
        when(operations.indexOps(any(Class.class))).thenReturn(indexOps);
        when(indexOps.exists()).thenReturn(false);

        indexer.indexEvent(event());

        verify(indexOps).createWithMapping();
        verify(operations).save(any(EventSearchDocument.class));
    }

    @Test
    void indexEventByIdLoadsAndSaves() {
        when(events.findById(42L)).thenReturn(java.util.Optional.of(event()));
        when(operations.indexOps(any(Class.class))).thenReturn(indexOps);
        when(indexOps.exists()).thenReturn(true);

        indexer.indexEventById(42L);

        verify(operations).save(any(EventSearchDocument.class));
    }

    @Test
    void indexEventSwallowsElasticsearchOutage() {
        when(operations.indexOps(any(Class.class)))
            .thenThrow(new RuntimeException("down"));

        assertThatNoException().isThrownBy(() -> indexer.indexEvent(event()));
    }
}
