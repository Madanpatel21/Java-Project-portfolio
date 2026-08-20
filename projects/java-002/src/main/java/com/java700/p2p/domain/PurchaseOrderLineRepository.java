package com.java700.p2p.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLine, String> {
    List<PurchaseOrderLine> findByPoId(String poId);
}
