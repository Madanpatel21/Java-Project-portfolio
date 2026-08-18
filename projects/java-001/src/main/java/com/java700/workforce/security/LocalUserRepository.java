package com.java700.workforce.security;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface LocalUserRepository extends JpaRepository<LocalUser, String> {

    Optional<LocalUser> findByUsername(String username);
}
