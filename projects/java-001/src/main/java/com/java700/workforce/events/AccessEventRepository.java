package com.java700.workforce.events;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccessEventRepository extends JpaRepository<AccessEvent, String> {

    boolean existsBySourceAndExternalId(String source, String externalId);

    @Query("select e.userId as userId, max(e.occurredAt) as last from AccessEvent e group by e.userId")
    List<LatestEvent> findLatestByUser();

    void deleteByOccurredAtBefore(Instant threshold);

    interface LatestEvent {
        String getUserId();

        Instant getLast();
    }
}
