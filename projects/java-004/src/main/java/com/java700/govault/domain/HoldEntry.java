package com.java700.govault.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** One document placed under a legal hold. */
@Entity
@Table(name = "hold_entries")
public class HoldEntry {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "hold_id", nullable = false, length = 36)
    private String holdId;

    @Column(name = "document_id", nullable = false, length = 36)
    private String documentId;

    protected HoldEntry() {
    }

    public HoldEntry(String id, String holdId, String documentId) {
        this.id = id;
        this.holdId = holdId;
        this.documentId = documentId;
    }

    public String getHoldId() {
        return holdId;
    }

    public String getDocumentId() {
        return documentId;
    }
}
