package beacon.event.api;

import beacon.event.api.dto.EventRequest;
import beacon.event.api.dto.EventResponse;
import beacon.event.application.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> receiveEvent(@Valid @RequestBody EventRequest request) {
        UUID eventId = eventService.processEvent(
                request.eventId(),
                request.aggregateId(),
                request.eventType(),
                request.payload(),
                request.occurredAt(),
                request.source(),
                request.actor(),
                request.changes() != null ? request.changes() : Collections.emptyList(),
                request.context()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.created(eventId));
    }
}