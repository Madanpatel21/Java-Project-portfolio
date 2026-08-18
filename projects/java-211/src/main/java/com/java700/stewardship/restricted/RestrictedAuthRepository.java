package com.java700.stewardship.restricted;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestrictedAuthRepository extends JpaRepository<RestrictedAuthorization, String> {

    List<RestrictedAuthorization> findByStatus(String status);

    List<RestrictedAuthorization> findByStatusAndExpiresAtBefore(String status, Instant at);
}
