package com.java700.expfraud.api;

import com.java700.expfraud.domain.FraudCase;
import com.java700.expfraud.security.SecurityUtil;
import com.java700.expfraud.service.Api;
import com.java700.expfraud.service.CaseService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Four-eyes fraud case workflow. */
@RestController
@RequestMapping("/api/v1/cases")
public class CaseController {

    private final CaseService cases;

    public CaseController(CaseService cases) {
        this.cases = cases;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FRAUD_INVESTIGATOR','AUDITOR','ADMIN')")
    public List<Api.CaseView> openCases() {
        return cases.openCases().stream().map(cases::view).toList();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('FRAUD_INVESTIGATOR','AUDITOR','ADMIN')")
    public List<Api.CaseView> pendingDecisions() {
        return cases.pendingDecisions().stream().map(cases::view).toList();
    }

    @GetMapping("/{caseId}")
    @PreAuthorize("hasAnyRole('FRAUD_INVESTIGATOR','AUDITOR','ADMIN')")
    public Api.CaseView get(@PathVariable String caseId) {
        return cases.view(cases.load(caseId));
    }

    @PostMapping("/{caseId}/review")
    @PreAuthorize("hasAnyRole('FRAUD_INVESTIGATOR','AUDITOR')")
    public Api.CaseView review(@PathVariable String caseId,
                               @Valid @RequestBody Api.CaseReviewRequest request) {
        FraudCase updated = cases.review(caseId, request.recommendation(), request.note(),
                SecurityUtil.currentUsername());
        return cases.view(updated);
    }

    @PostMapping("/{caseId}/decide")
    @PreAuthorize("hasAnyRole('FRAUD_INVESTIGATOR','AUDITOR')")
    public Api.CaseView decide(@PathVariable String caseId,
                               @Valid @RequestBody Api.CaseDecisionRequest request) {
        FraudCase updated = cases.decide(caseId, request.decision(), request.note(),
                SecurityUtil.currentUsername());
        return cases.view(updated);
    }
}
