package com.java700.legalmatter.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** An ethical wall: roles excluded from a matter. */
@Entity
@Table(name = "ethical_walls")
public class EthicalWall {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "matter_id", nullable = false, length = 36)
    private String matterId;

    @Column(name = "role_name", nullable = false, length = 64)
    private String roleName;

    protected EthicalWall() {
    }

    public EthicalWall(String id, String matterId, String roleName) {
        this.id = id;
        this.matterId = matterId;
        this.roleName = roleName;
    }

    public String getMatterId() {
        return matterId;
    }

    public String getRoleName() {
        return roleName;
    }
}
