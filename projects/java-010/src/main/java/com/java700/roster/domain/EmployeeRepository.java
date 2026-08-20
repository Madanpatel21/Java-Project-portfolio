package com.java700.roster.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Employee persistence. */
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    List<Employee> findByActiveTrue();

    Optional<Employee> findByUserId(String userId);
}
