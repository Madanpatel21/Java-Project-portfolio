package com.java700.stewardship.catalog;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrugRepository extends JpaRepository<AntimicrobialDrug, String> {

    Optional<AntimicrobialDrug> findByCode(String code);
}
