package com.java700.fleetmaint.security;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalUserRoleRepository extends JpaRepository<LocalUserRole, LocalUserRole.Key> {

    List<LocalUserRole> findByUserId(String userId);
}
