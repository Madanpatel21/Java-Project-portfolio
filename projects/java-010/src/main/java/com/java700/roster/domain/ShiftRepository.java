package com.java700.roster.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Shift persistence. */
public interface ShiftRepository extends JpaRepository<Shift, String> {

    List<Shift> findByRosterIdOrderByShiftDateAsc(String rosterId);
}
