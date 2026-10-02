package com.grabseat.search;

import com.grabseat.dto.EventSummaryResponse;
import com.grabseat.model.EventType;
import com.grabseat.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Elasticsearch-first search with Postgres fallback: if ES is unreachable or
 * empty, the DB query answers so search never goes down with the index.
 */
@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final ElasticsearchOperations operations;
    private final EventSearchIndexer indexer;
    private final EventService eventService;

    public SearchService(ElasticsearchOperations operations, EventSearchIndexer indexer,
                         EventService eventService) {
        this.operations = operations;
        this.indexer = indexer;
        this.eventService = eventService;
    }

    public Page<EventSummaryResponse> search(String keyword, LocalDateTime start,
                                            LocalDateTime end, int page, int pageSize) {
        log.info("Searching events with keyword='{}', start={}, end={}, page={}, pageSize={}",
                                                keyword, start, end, page, pageSize);
        try {
            if (!indexer.indexHasData()) {
                log.info("Elasticsearch index is empty, using database fallback");
                return eventService.search(keyword, start, end, page, pageSize);
            }
            return searchIndex(keyword, start, end, page, pageSize);
        } catch (Exception e) {
            log.warn("Elasticsearch search failed, using database fallback: {}", e.toString());
            return eventService.search(keyword, start, end, page, pageSize);
        }
    }

    private Page<EventSummaryResponse> searchIndex(String keyword, LocalDateTime start,
                                                   LocalDateTime end, int page, int pageSize) {
        log.info("Searching events in Elasticsearch index with keyword='{}', start={}, end={}, page={}, pageSize={}",
                keyword, start, end, page, pageSize);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("startTime").ascending());

        Criteria criteria = new Criteria();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            criteria = new Criteria("name").contains(kw)
                .or(new Criteria("description").contains(kw))
                .or(new Criteria("venueName").contains(kw))
                .or(new Criteria("performerName").contains(kw))
                .or(new Criteria("screenName").contains(kw));
        }
        if (start != null) {
            criteria = criteria.and(new Criteria("startTime").greaterThanEqual(start));
        }
        if (end != null) {
            criteria = criteria.and(new Criteria("startTime").lessThanEqual(end));
        }
        CriteriaQuery query = new CriteriaQuery(criteria, pageable);
        var hits = operations.search(query, EventSearchDocument.class);
        return SearchHitSupport.searchPageFor(hits, pageable)
            .map(hit -> toSummary(hit.getContent()));
    }

    private EventSummaryResponse toSummary(EventSearchDocument d) {
        log.info("Mapping EventSearchDocument to EventSummaryResponse: {}", d);
        return new EventSummaryResponse(
            d.getEventId(), d.getName(), d.getDescription(),
            d.getType() != null ? EventType.valueOf(d.getType()) : null,
            d.getStartTime(), d.getEndTime(),
            d.getBasePrice() != null ? BigDecimal.valueOf(d.getBasePrice()) : null,
            d.getVenueName(), d.getPerformerName(), d.getScreenName());
    }
}
