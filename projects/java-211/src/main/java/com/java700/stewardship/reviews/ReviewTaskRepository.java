package com.java700.stewardship.reviews;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewTaskRepository extends JpaRepository<ReviewTask, String> {

    List<ReviewTask> findByStatusOrderByDueAtAsc(String status);

    Optional<ReviewTask> findByPrescriptionIdAndStatusAndTriggerReason(
            String prescriptionId, String status, String triggerReason);

    List<ReviewTask> findByPrescriptionIdAndStatus(String prescriptionId, String status);
}
