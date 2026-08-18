package com.java700.crvs.dedup;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DedupRepository extends JpaRepository<DedupCandidate, String> {

    List<DedupCandidate> findByStatus(String status);

    boolean existsByPersonAIdAndPersonBIdAndStatus(String a, String b, String status);
}
