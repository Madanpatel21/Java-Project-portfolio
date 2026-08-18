package com.java700.crvs.ledger;

import com.java700.crvs.common.api.Problems;
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
@RequestMapping("/api/v1/ledger")
@Tag(name = "ledger", description = "Dual hash-chained life-event ledger (global + per-person)")
public class LedgerController {

    private final LedgerService service;

    public LedgerController(LedgerService service) {
        this.service = service;
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify the global chain; use ?recent=N for a tail check")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','STATISTICIAN')")
    public LedgerApi.VerificationView verify(@RequestParam(required = false) Integer recent) {
        if (recent != null && recent < 1) {
            throw new Problems.BadRequest("recent must be >= 1");
        }
        HashChain.Verification v = recent == null
                ? service.verifyGlobal() : service.verifyGlobalRecent(recent);
        return new LedgerApi.VerificationView(v.valid(), v.entriesChecked(), v.brokenSeq(),
                v.expectedHash(), v.actualHash());
    }

    @GetMapping("/person/{personId}")
    @Operation(summary = "A person's full life-event history (chain-ordered)")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','REGISTRAR','STATISTICIAN')")
    public List<LedgerApi.EventView> history(@PathVariable String personId) {
        return service.personHistory(personId).stream().map(LedgerApi.EventView::from).toList();
    }

    @GetMapping("/person/{personId}/verify")
    @Operation(summary = "Verify a person's per-person chain integrity")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','STATISTICIAN')")
    public LedgerApi.VerificationView verifyPerson(@PathVariable String personId) {
        HashChain.Verification v = service.verifyPersonChain(personId);
        return new LedgerApi.VerificationView(v.valid(), v.entriesChecked(), v.brokenSeq(),
                v.expectedHash(), v.actualHash());
    }
}
