package com.java700.roster.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** ShiftAssignment persistence. */
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, String> {

    List<ShiftAssignment> findByRosterId(String rosterId);

    List<ShiftAssignment> findByRosterIdAndEmployeeId(String rosterId, String employeeId);

    List<ShiftAssignment> findByRosterIdAndStatus(String rosterId, String status);
}
