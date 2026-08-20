package com.java700.expfraud.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Whistleblower tip persistence. */
public interface TipRepository extends JpaRepository<Tip, String> {

    Optional<Tip> findByTipNo(String tipNo);

    List<Tip> findByStatusOrderBySubmittedAtAsc(String status);
}
