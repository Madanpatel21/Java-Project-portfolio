package com.java700.p2p.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> {
    Optional<PurchaseOrder> findByPoNumber(String poNumber);

        List<PurchaseOrder> findBySupplierId(String supplierId);
}
