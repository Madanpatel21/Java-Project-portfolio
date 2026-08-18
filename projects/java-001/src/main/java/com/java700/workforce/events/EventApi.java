package com.java700.workforce.events;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class EventApi {

    private EventApi() {
    }

    public record IngestRequest(
            @NotBlank String userId,
            @NotBlank @Size(max = 120) String resourceName,
            @NotBlank @Size(max = 32) String eventType,
            @Size(max = 64) String ipAddress,
            @NotBlank @Size(max = 64) String source,
            @Size(max = 128) String externalId,
            Instant occurredAt) {
    }

    public record IngestResponse(String eventId, boolean duplicate) {
    }
}
