package com.java700.legalmatter.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "local_user_roles")
@IdClass(LocalUserRole.Key.class)
public class LocalUserRole {

    @Id
    @Column(name = "user_id", length = 36)
    private String userId;

    @Id
    @Column(name = "role_name", length = 64)
    private String roleName;

    protected LocalUserRole() {
    }

    public LocalUserRole(String userId, String roleName) {
        this.userId = userId;
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public static class Key implements Serializable {
        private String userId;
        private String roleName;

        public Key() {
        }

        public Key(String userId, String roleName) {
            this.userId = userId;
            this.roleName = roleName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Key key)) {
                return false;
            }
            return Objects.equals(userId, key.userId) && Objects.equals(roleName, key.roleName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, roleName);
        }
    }
}
