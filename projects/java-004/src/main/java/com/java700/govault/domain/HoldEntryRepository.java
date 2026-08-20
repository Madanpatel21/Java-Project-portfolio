package com.java700.govault.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldEntryRepository extends JpaRepository<HoldEntry, String> {
    List<HoldEntry> findByHoldId(String holdId);

        List<HoldEntry> findByDocumentId(String documentId);

        Optional<HoldEntry> findByHoldIdAndDocumentId(String holdId, String documentId);
}
