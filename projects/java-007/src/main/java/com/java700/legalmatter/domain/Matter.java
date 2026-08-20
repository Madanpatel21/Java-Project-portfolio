package com.java700.legalmatter.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

/** A legal matter (case/engagement) owned by a client party. */
@Entity
@Table(name = "matters")
public class Matter {

    public enum Status {
        OPEN, CLOSED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "matter_no", nullable = false, unique = true, length = 40)
    private String matterNo;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "client_party_id", nullable = false, length = 36)
    private String clientPartyId;

    @Column(name = "practice_area", length = 64)
    private String practiceArea;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Matter() {
    }

    public Matter(String id, String matterNo, String name, String clientPartyId,
                  String practiceArea, Instant openedAt) {
        this.id = id;
        this.matterNo = matterNo;
        this.name = name;
        this.status = Status.OPEN.name();
        this.clientPartyId = clientPartyId;
        this.practiceArea = practiceArea;
        this.openedAt = openedAt;
    }

    public String getId() {
        return id;
    }

    public String getMatterNo() {
        return matterNo;
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public String getClientPartyId() {
        return clientPartyId;
    }

    public String getPracticeArea() {
        return practiceArea;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void close(Instant at) {
        this.status = Status.CLOSED.name();
        this.closedAt = at;
    }
}
