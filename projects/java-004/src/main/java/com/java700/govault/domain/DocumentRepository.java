package com.java700.govault.domain;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, String> {
    Page<Document> findByStatus(String status, Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("update Document d set d.uploadedAt = :at where d.id = :id")
    void backdateUploadedAt(@org.springframework.data.repository.query.Param("id") String id,
                            @org.springframework.data.repository.query.Param("at") Instant at);

        List<Document> findByStatusAndUploadedAtBefore(String status, Instant at);

        Page<Document> findByTitleContainingIgnoreCaseOrExtractedTextContainingIgnoreCase(
                String title, String text, Pageable pageable);

        List<Document> findByStatus(String status);
}
