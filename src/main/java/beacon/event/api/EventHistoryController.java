package beacon.event.api;

import beacon.common.exception.EventNotFoundException;
import beacon.event.api.dto.EventHistoryResponse;
import beacon.event.api.dto.EventSummary;
import beacon.event.application.EventService;
import beacon.event.domain.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/aggregates")
@RequiredArgsConstructor
public class EventHistoryController {

    private final EventService eventService;

    @GetMapping("/{aggregateId}/history")
    public ResponseEntity<EventHistoryResponse> getHistory(@PathVariable String aggregateId) {
        List<Event> events = eventService.getAggregateHistory(aggregateId);

        if (events.isEmpty()) {
            throw new EventNotFoundException(aggregateId);
        }

        List<EventSummary> summaries = events.stream()
                .map(event -> EventSummary.builder()
                        .eventId(event.getEventId())
                        .eventType(event.getEventType())
                        .payload(event.getPayload())
                        .occurredAt(event.getOccurredAt())
                        .receivedAt(event.getReceivedAt())
                        .source(event.getSource())
                        .build())
                .toList();

        return ResponseEntity.ok(EventHistoryResponse.builder()
                .aggregateId(aggregateId)
                .events(summaries)
                .build());
    }
}