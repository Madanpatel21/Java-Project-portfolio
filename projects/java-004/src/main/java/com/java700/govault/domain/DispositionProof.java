package com.java700.govault.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Append-only disposition record: what was destroyed, when, by whom, with the content hash. */
@Entity
@Table(name = "disposition_proofs")
public class DispositionProof {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "document_id", nullable = false, length = 36)
    private String documentId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "retention_class", nullable = false, length = 8)
    private String retentionClass;

    @Column(name = "disposed_at", nullable = false)
    private Instant disposedAt;

    @Column(name = "executor", nullable = false, length = 120)
    private String executor;

    @Column(name = "disposition", nullable = false, length = 16)
    private String disposition;

    protected DispositionProof() {
    }

    public DispositionProof(String id, String documentId, String title, String contentHash,
                            String retentionClass, Instant disposedAt, String executor,
                            String disposition) {
        this.id = id;
        this.documentId = documentId;
        this.title = title;
        this.contentHash = contentHash;
        this.retentionClass = retentionClass;
        this.disposedAt = disposedAt;
        this.executor = executor;
        this.disposition = disposition;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getRetentionClass() {
        return retentionClass;
    }

    public String getContentHash() {
        return contentHash;
    }

    public Instant getDisposedAt() {
        return disposedAt;
    }

    public String getExecutor() {
        return executor;
    }

    public String getDisposition() {
        return disposition;
    }
}
