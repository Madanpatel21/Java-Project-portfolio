package com.java700.stewardship.microbiology;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IsolateRepository extends JpaRepository<Isolate, String> {

    List<Isolate> findByCultureId(String cultureId);
}
