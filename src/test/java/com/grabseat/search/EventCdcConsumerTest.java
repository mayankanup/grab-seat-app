package com.grabseat.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventCdcConsumerTest {

    @Mock
    EventSearchIndexer indexer;

    private final ObjectMapper mapper = new ObjectMapper();

    private EventCdcConsumer consumer() {
        return new EventCdcConsumer(mapper, indexer);
    }

    @Test
    void createMessageIndexesEventById() throws Exception {
        consumer().handle("{\"payload\":{\"before\":null,"
            + "\"after\":{\"id\":42,\"name\":\"Dune\"},\"op\":\"c\"}}");

        verify(indexer).indexEventById(42L);
    }

    @Test
    void deleteMessageDeletesDocument() throws Exception {
        consumer().handle("{\"payload\":{\"before\":{\"id\":42},\"after\":null,\"op\":\"d\"}}");

        verify(indexer).deleteEvent(42L);
    }

    @Test
    void poisonMessageIsSwallowedByListener() {
        assertThatNoException().isThrownBy(() -> consumer().onMessage("k", "not-json{{{"));
        verifyNoInteractions(indexer);
    }
}
