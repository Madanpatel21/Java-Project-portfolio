package com.java700.workforce.evidence;

import com.java700.workforce.common.api.Problems;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/evidence")
@Tag(name = "evidence", description = "Hash-chained, tamper-evident compliance evidence ledger")
public class EvidenceController {

    private final EvidenceService service;

    public EvidenceController(EvidenceService service) {
        this.service = service;
    }

    @GetMapping("/{seq}")
    @Operation(summary = "Fetch one ledger entry by sequence number")
    @PreAuthorize("hasAnyRole('AUDITOR','COMPLIANCE_ADMIN','COMPLIANCE_OFFICER','ACCESS_MANAGER')")
    public EvidenceApi.EntryView bySeq(@PathVariable long seq) {
        return EvidenceApi.EntryView.from(service.bySeq(seq));
    }

    @GetMapping
    @Operation(summary = "List evidence entries for an aggregate")
    @PreAuthorize("hasAnyRole('AUDITOR','COMPLIANCE_ADMIN','COMPLIANCE_OFFICER','ACCESS_MANAGER')")
    public List<EvidenceApi.EntryView> entries(@RequestParam String aggregateType,
                                               @RequestParam String aggregateId) {
        return service.entries(aggregateType, aggregateId).stream()
                .map(EvidenceApi.EntryView::from)
                .toList();
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify ledger integrity; use ?recent=N to check only the newest N links")
    @PreAuthorize("hasAnyRole('AUDITOR','COMPLIANCE_ADMIN','COMPLIANCE_OFFICER')")
    public EvidenceApi.VerificationView verify(@RequestParam(required = false) Integer recent) {
        if (recent != null && recent < 1) {
            throw new Problems.BadRequest("recent must be >= 1");
        }
        HashChain.Verification v = recent == null ? service.verifyChain() : service.verifyRecent(recent);
        return new EvidenceApi.VerificationView(v.valid(), v.entriesChecked(), v.brokenSeq(),
                v.expectedHash(), v.actualHash());
    }
}
