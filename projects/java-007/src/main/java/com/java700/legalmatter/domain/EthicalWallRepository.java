package com.java700.legalmatter.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EthicalWallRepository extends JpaRepository<EthicalWall, String> {
    List<EthicalWall> findByMatterId(String matterId);
}
