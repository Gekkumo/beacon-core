package beacon.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEventException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateEventException ex) {
        Map<String, Object> body = Map.of(
                "error", "Duplicate event",
                "eventId", ex.getEventId().toString(),
                "timestamp", Instant.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EventNotFoundException ex) {
        Map<String, Object> body = Map.of(
                "error", "Not found",
                "aggregateId", ex.getAggregateId(),
                "timestamp", Instant.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(EventProcessingException.class)
    public ResponseEntity<Map<String, Object>> handleProcessing(EventProcessingException ex) {
        log.error("Event processing failed", ex);
        Map<String, Object> body = Map.of(
                "error", "Processing failed",
                "timestamp", Instant.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = Map.of(
                "error", "Validation failed",
                "details", ex.getBindingResult().getFieldErrors().stream()
                        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                        .toList(),
                "timestamp", Instant.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex);
        Map<String, Object> body = Map.of(
                "error", "Internal server error",
                "timestamp", Instant.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}