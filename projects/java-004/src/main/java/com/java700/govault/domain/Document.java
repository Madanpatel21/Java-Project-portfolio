package com.java700.govault.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

/** A governed document with classification, retention class and legal-hold state. */
@Entity
@Table(name = "documents")
public class Document {

    public enum Classification {
        UNCLASSIFIED, PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    }

    public enum Status {
        ACTIVE, QUARANTINED, DISPOSED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", length = 64)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "classification", nullable = false, length = 16)
    private String classification;

    @Column(name = "retention_class", nullable = false, length = 8)
    private String retentionClass;

    @Column(name = "owner_id", nullable = false, length = 36)
    private String ownerId;

    @Column(name = "owner_name", nullable = false, length = 120)
    private String ownerName;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "extracted_text", length = 8000)
    private String extractedText;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "legal_hold", nullable = false)
    private boolean legalHold;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @Column(name = "disposed_at")
    private Instant disposedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Document() {
    }

    public Document(String id, String title, String fileName, String contentType, long sizeBytes,
                    String ownerId, String ownerName, String contentHash, String extractedText,
                    Instant uploadedAt) {
        this.id = id;
        this.title = title;
        this.fileName = fileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.classification = Classification.UNCLASSIFIED.name();
        this.retentionClass = "R1";
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.contentHash = contentHash;
        this.extractedText = extractedText;
        this.status = Status.QUARANTINED.name();
        this.legalHold = false;
        this.uploadedAt = uploadedAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public Classification getClassification() {
        return Classification.valueOf(classification);
    }

    public String getRetentionClass() {
        return retentionClass;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getContentHash() {
        return contentHash;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public boolean isLegalHold() {
        return legalHold;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public Instant getDisposedAt() {
        return disposedAt;
    }

    public void classify(Classification newClassification, String newRetentionClass) {
        this.classification = newClassification.name();
        this.retentionClass = newRetentionClass;
        if (Status.QUARANTINED.name().equals(this.status)) {
            this.status = Status.ACTIVE.name();
        }
    }

    public void placeHold() {
        this.legalHold = true;
    }

    public void releaseHold() {
        this.legalHold = false;
    }

    public void dispose(Instant at) {
        this.status = Status.DISPOSED.name();
        this.disposedAt = at;
    }

    public void quarantine() {
        this.status = Status.QUARANTINED.name();
    }
}
