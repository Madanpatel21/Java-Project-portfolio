package com.java700.p2p.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, String> {
    List<GoodsReceipt> findByPoId(String poId);

        Optional<GoodsReceipt> findByGrNumber(String grNumber);
}
