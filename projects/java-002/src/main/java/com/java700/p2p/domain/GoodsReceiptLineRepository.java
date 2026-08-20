package com.java700.p2p.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodsReceiptLineRepository extends JpaRepository<GoodsReceiptLine, String> {
    List<GoodsReceiptLine> findByGrId(String grId);
}
