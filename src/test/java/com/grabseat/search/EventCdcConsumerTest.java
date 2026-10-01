package com.grabseat.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grabseat.model.*;
import com.grabseat.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventCdcConsumerTest {

    @Mock
    EventRepository events;
    @Mock
    EventSearchIndexer indexer;

    private final ObjectMapper mapper = new ObjectMapper();

    private EventCdcConsumer consumer() {
        return new EventCdcConsumer(mapper, events, indexer);
    }

    private Event event() {
        Venue venue = new Venue("PVR", "Mall", 120);
        return new Event("Dune", "IMAX", EventType.MOVIE, venue, null,
            LocalDateTime.now().plusDays(1), null, new BigDecimal("349.00"));
    }

    @Test
    void createMessageIndexesEvent() throws Exception {
        when(events.findById(42L)).thenReturn(Optional.of(event()));

        consumer().handle("{\"payload\":{\"before\":null,"
            + "\"after\":{\"id\":42,\"name\":\"Dune\"},\"op\":\"c\"}}");

        verify(indexer).indexEvent(any(Event.class));
    }

    @Test
    void deleteMessageDeletesDocument() throws Exception {
        consumer().handle("{\"payload\":{\"before\":{\"id\":42},\"after\":null,\"op\":\"d\"}}");

        verify(indexer).deleteEvent(42L);
        verify(events, never()).findById(anyLong());
    }

    @Test
    void poisonMessageIsSwallowedByListener() {
        assertThatNoException().isThrownBy(() -> consumer().onMessage("k", "not-json{{{"));
        verifyNoInteractions(indexer);
    }
}
