package com.java700.crvs.certificates;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, String> {

    Optional<Certificate> findByToken(String token);

    List<Certificate> findByPersonIdOrderByIssuedAtDesc(String personId);
}
