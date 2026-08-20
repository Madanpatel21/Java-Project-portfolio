package com.java700.fleetmaint.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** PartReservation persistence. */
public interface PartReservationRepository extends JpaRepository<PartReservation, String> {

    List<PartReservation> findByWorkOrderId(String workOrderId);

    List<PartReservation> findByWorkOrderIdAndStatus(String workOrderId, String status);
}
