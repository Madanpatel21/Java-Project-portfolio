package com.java700.crvs.registry;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public final class RegistryRepositories {

    private RegistryRepositories() {
    }

    public interface PersonRepository extends JpaRepository<Person, String> {

        Optional<Person> findByNationalId(String nationalId);

        Page<Person> findByFullNameContainingIgnoreCase(String query, Pageable pageable);

        List<Person> findByDobAndSex(java.time.LocalDate dob, String sex);

        List<Person> findByStatus(String status);

        List<Person> findByRegion(String region);
    }

}
