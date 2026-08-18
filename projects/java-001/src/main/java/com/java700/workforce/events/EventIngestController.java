package com.java700.workforce.events;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "events", description = "Access-event ingestion (rate-limited, idempotent)")
public class EventIngestController {

    private final EventIngestService service;

    public EventIngestController(EventIngestService service) {
        this.service = service;
    }

    @PostMapping("/access")
    @Operation(summary = "Ingest an access event (systems/integrations only)")
    @PreAuthorize("hasAnyRole('INTEGRATION','COMPLIANCE_ADMIN')")
    public EventApi.IngestResponse ingest(@Valid @RequestBody EventApi.IngestRequest req,
                                          @RequestHeader(value = "Idempotency-Key") String idempotencyKey) {
        return service.ingest(req, idempotencyKey);
    }
}
