package com.java700.crvs.offices;

import com.java700.crvs.registry.Office;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfficeRepository extends JpaRepository<Office, String> {

    Optional<Office> findByCode(String code);
}
