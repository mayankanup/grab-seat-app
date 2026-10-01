package com.grabseat.search;

import com.grabseat.dto.EventSummaryResponse;
import com.grabseat.service.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    ElasticsearchOperations operations;
    @Mock
    EventSearchIndexer indexer;
    @Mock
    EventService eventService;

    @InjectMocks
    SearchService service;

    private EventSearchDocument doc() {
        return new EventSearchDocument(1L, "Dune Evening Show", "IMAX sci-fi", "MOVIE",
            LocalDateTime.parse("2026-10-02T18:00:00"),
            LocalDateTime.parse("2026-10-02T21:00:00"), 349.0,
            "PVR Downtown Cinema", "Dune Cast", "IMAX");
    }

    @Test
    void answersFromElasticsearchWhenAvailable() {
        when(indexer.indexHasData()).thenReturn(true);
        SearchHit<EventSearchDocument> hit = mock(SearchHit.class);
        when(hit.getContent()).thenReturn(doc());
        SearchHits<EventSearchDocument> hits = mock(SearchHits.class);
        when(hits.getSearchHits()).thenReturn(List.of(hit));
        when(hits.getTotalHits()).thenReturn(1L);
        when(operations.search(any(Query.class), eq(EventSearchDocument.class)))
            .thenReturn(hits);

        Page<EventSummaryResponse> page = service.search("dune", null, null, 0, 20);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).name()).isEqualTo("Dune Evening Show");
        assertThat(page.getContent().get(0).screenName()).isEqualTo("IMAX");
        verify(eventService, never()).search(any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void fallsBackToDatabaseWhenIndexEmpty() {
        when(indexer.indexHasData()).thenReturn(false);
        Page<EventSummaryResponse> db = new PageImpl<>(List.of());
        when(eventService.search(eq("dune"), any(), any(), eq(0), eq(20))).thenReturn(db);

        assertThat(service.search("dune", null, null, 0, 20)).isSameAs(db);
        verify(operations, never()).search(any(Query.class), eq(EventSearchDocument.class));
    }

    @Test
    void fallsBackToDatabaseWhenElasticsearchFails() {
        when(indexer.indexHasData()).thenReturn(true);
        when(operations.search(any(Query.class), eq(EventSearchDocument.class)))
            .thenThrow(new RuntimeException("connection refused"));
        Page<EventSummaryResponse> db = new PageImpl<>(List.of());
        when(eventService.search(eq("dune"), any(), any(), eq(0), eq(20))).thenReturn(db);

        assertThat(service.search("dune", null, null, 0, 20)).isSameAs(db);
    }
}
