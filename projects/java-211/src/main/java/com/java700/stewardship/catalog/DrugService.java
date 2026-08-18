package com.java700.stewardship.catalog;

import com.java700.stewardship.common.api.Problems;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DrugService {

    private final DrugRepository repository;

    public DrugService(DrugRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public AntimicrobialDrug get(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Drug not found"));
    }

    @Transactional(readOnly = true)
    public AntimicrobialDrug byCode(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new Problems.NotFound("Drug not found: " + code));
    }

    @Transactional(readOnly = true)
    public List<DrugApi.DrugView> list() {
        return repository.findAll().stream().map(DrugApi.DrugView::from).toList();
    }
}
