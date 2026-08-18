package com.java700.stewardship.guidelines;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuidelineRepository extends JpaRepository<StewardshipGuideline, String> {

    Optional<StewardshipGuideline> findByStatus(String status);

    List<StewardshipGuideline> findAllByOrderByVersionNoDesc();
}
