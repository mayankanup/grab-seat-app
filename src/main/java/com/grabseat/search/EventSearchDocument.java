package com.grabseat.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "events")
public class EventSearchDocument {

    @Id
    private String id;
    private Long eventId;
    @Field(type = FieldType.Text)
    private String name;
    @Field(type = FieldType.Text)
    private String description;
    private String type;
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime startTime;
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime endTime;
    private Double basePrice;
    @Field(type = FieldType.Text)
    private String venueName;
    @Field(type = FieldType.Text)
    private String performerName;
    @Field(type = FieldType.Text)
    private String screenName;

    public EventSearchDocument() {
    }

    public EventSearchDocument(Long eventId, String name, String description, String type,
                               LocalDateTime startTime, LocalDateTime endTime, Double basePrice,
                               String venueName, String performerName, String screenName) {
        this.id = String.valueOf(eventId);
        this.eventId = eventId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.basePrice = basePrice;
        this.venueName = venueName;
        this.performerName = performerName;
        this.screenName = screenName;
    }

    public String getId() { return id; }
    public Long getEventId() { return eventId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Double getBasePrice() { return basePrice; }
    public String getVenueName() { return venueName; }
    public String getPerformerName() { return performerName; }
    public String getScreenName() { return screenName; }
}
