package com.java700.roster.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Roster persistence. */
public interface RosterRepository extends JpaRepository<Roster, String> {

    List<Roster> findByStatusOrderByCreatedAtDesc(String status);

    List<Roster> findAllByOrderByCreatedAtDesc();
}
