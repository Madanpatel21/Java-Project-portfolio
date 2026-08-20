package com.java700.legalmatter.domain;


import org.springframework.data.jpa.repository.JpaRepository;

public interface ConflictCheckRepository extends JpaRepository<ConflictCheck, String> {
    
}
