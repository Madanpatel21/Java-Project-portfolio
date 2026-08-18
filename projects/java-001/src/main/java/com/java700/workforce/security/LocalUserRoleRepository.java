package com.java700.workforce.security;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface LocalUserRoleRepository extends JpaRepository<LocalUserRole, LocalUserRole.Key> {

    List<LocalUserRole> findByUserId(String userId);
}
