package com.java700.expfraud.security;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalUserRepository extends JpaRepository<LocalUser, String> {

    Optional<LocalUser> findByUsername(String username);
}
