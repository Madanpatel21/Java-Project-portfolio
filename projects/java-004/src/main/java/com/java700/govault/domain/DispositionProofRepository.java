package com.java700.govault.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispositionProofRepository extends JpaRepository<DispositionProof, String> {
    List<DispositionProof> findByDocumentId(String documentId);
}
