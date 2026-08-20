package com.java700.legalmatter.api;

import com.java700.legalmatter.service.Api;
import com.java700.legalmatter.service.LegalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "legal", description = "Matters, party-graph conflict screening, court deadlines, ethical walls")
public class LegalController {

    private final LegalService service;

    public LegalController(LegalService service) {
        this.service = service;
    }

    @PostMapping("/parties")
    @Operation(summary = "Register a party (CLIENT / OPPONENT / RELATED)")
    @PreAuthorize("hasAnyRole('ATTORNEY','PARALEGAL','ADMIN')")
    public String createParty(@Valid @RequestBody Api.CreatePartyRequest body,
                              @RequestHeader(value = "Idempotency-Key") String idemKey) {
        return service.createParty(body.name(), body.type(), idemKey);
    }

    @GetMapping("/parties")
    @Operation(summary = "List parties")
    @PreAuthorize("isAuthenticated()")
    public List<Api.PartyView> parties() {
        return service.parties();
    }

    @PostMapping("/matters")
    @Operation(summary = "Open a matter for a client party")
    @PreAuthorize("hasAnyRole('ATTORNEY','PARALEGAL','ADMIN')")
    public String createMatter(@Valid @RequestBody Api.CreateMatterRequest body,
                               @RequestHeader(value = "Idempotency-Key") String idemKey) {
        return service.createMatter(body.matterNo(), body.name(), body.clientPartyId(),
                body.practiceArea(), idemKey);
    }

    @PostMapping("/matters/{id}/parties")
    @Operation(summary = "Add a party to a matter (OPPOSING / ADVERSE / WITNESS)")
    @PreAuthorize("hasAnyRole('ATTORNEY','PARALEGAL','ADMIN')")
    public void addMatterParty(@PathVariable String id,
                               @Valid @RequestBody Api.AddMatterPartyRequest body) {
        service.addMatterParty(id, body.partyId(), body.role());
    }

    @GetMapping("/matters/{id}")
    @Operation(summary = "Matter view (ethical walls enforced)")
    @PreAuthorize("isAuthenticated()")
    public Api.MatterView matter(@PathVariable String id) {
        return service.matterView(id);
    }

    @PostMapping("/conflicts/screen")
    @Operation(summary = "Screen a prospective client against adverse parties (graph walk)")
    @PreAuthorize("hasAnyRole('CONFLICT_ANALYST','ATTORNEY','ADMIN')")
    public Api.ScreenView screen(@Valid @RequestBody Api.ScreenRequest body,
                                 @RequestHeader(value = "Idempotency-Key") String idemKey) {
        return service.screen(body.subjectName(), body.adverseNames(), idemKey);
    }

    @PostMapping("/matters/{id}/deadlines")
    @Operation(summary = "Compute court deadlines from rules (jurisdiction + trigger date)")
    @PreAuthorize("hasAnyRole('PARALEGAL','ATTORNEY','ADMIN')")
    public List<Api.DeadlineView> computeDeadlines(@PathVariable String id,
                                                   @Valid @RequestBody Api.ComputeDeadlinesRequest body,
                                                   @RequestHeader(value = "Idempotency-Key") String idemKey) {
        return service.computeDeadlines(id, body.jurisdiction(), body.triggerDate(), idemKey);
    }

    @GetMapping("/matters/{id}/deadlines")
    @Operation(summary = "List a matter's deadlines")
    @PreAuthorize("isAuthenticated()")
    public List<Api.DeadlineView> deadlines(@PathVariable String id) {
        return service.deadlines(id);
    }

    @PostMapping("/deadlines/{id}/complete")
    @Operation(summary = "Complete a deadline")
    @PreAuthorize("hasAnyRole('PARALEGAL','ATTORNEY','ADMIN')")
    public Api.DeadlineView complete(@PathVariable String id) {
        return service.completeDeadline(id);
    }

    @PostMapping("/deadlines/mark-missed")
    @Operation(summary = "Mark past-due deadlines as MISSED")
    @PreAuthorize("hasAnyRole('PARALEGAL','ADMIN')")
    public int markMissed() {
        return service.markMissed();
    }

    @PostMapping("/matters/{id}/walls")
    @Operation(summary = "Add an ethical wall (role excluded from the matter)")
    @PreAuthorize("hasRole('ADMIN')")
    public void addWall(@PathVariable String id, @RequestBody java.util.Map<String, String> body) {
        service.addWall(id, body.get("roleName"));
    }
}
