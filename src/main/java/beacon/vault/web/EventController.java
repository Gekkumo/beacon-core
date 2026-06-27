package beacon.vault.web;

import beacon.vault.api.EventRequest;
import beacon.vault.api.EventResponse;
import beacon.vault.api.EventHistoryResponse;
import beacon.vault.api.EventSummary;
import beacon.vault.domain.*;
import beacon.vault.domain.vo.Actor;
import beacon.vault.domain.vo.Source;
import beacon.vault.application.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> receiveEvent(@Valid @RequestBody EventRequest request) {
        Event event = Event.createRaw(
                AggregateId.of(request.aggregateId()),
                EventType.of(request.eventType()),
                request.payload(),
                request.occurredAt(),
                Source.of(request.source()),
                request.actor() != null ? request.actor() : Actor.system(),
                request.changes(),
                request.context(),
                request.traceId() != null ? TraceId.of(request.traceId()) : null
        );

        EventId eventId = eventService.saveEvent(event);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EventResponse.created(eventId.value()));
    }

    @GetMapping("/aggregates/{aggregateId}/history")
    public ResponseEntity<EventHistoryResponse> getHistory(@PathVariable String aggregateId) {
        List<Event> events = eventService.getAggregateHistory(AggregateId.of(aggregateId));

        List<EventSummary> summaries = events.stream()
                .map(event -> EventSummary.builder()
                        .eventId(event.eventId().value())
                        .eventType(event.eventType().value())
                        .payload(event.payload())
                        .occurredAt(event.occurredAt())
                        .receivedAt(event.receivedAt())
                        .source(event.source().value())
                        .build())
                .toList();

        return ResponseEntity.ok(EventHistoryResponse.builder()
                .aggregateId(aggregateId)
                .events(summaries)
                .build());
    }
}