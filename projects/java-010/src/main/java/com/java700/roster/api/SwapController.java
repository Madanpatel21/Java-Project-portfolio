package com.java700.roster.api;

import com.java700.roster.security.SecurityUtil;
import com.java700.roster.service.Api;
import com.java700.roster.service.SwapService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Manager swap approval queue. */
@RestController
@RequestMapping("/api/v1/swaps")
public class SwapController {

    private final SwapService swaps;

    public SwapController(SwapService swaps) {
        this.swaps = swaps;
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public List<Api.SwapRequestView> pending() {
        return swaps.pending().stream().map(swaps::view).toList();
    }

    @PostMapping("/{swapId}/decide")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Api.SwapRequestView decide(@PathVariable String swapId,
                                      @Valid @RequestBody Api.SwapDecisionRequest request) {
        return swaps.view(swaps.decide(swapId, request.decision(),
                SecurityUtil.currentUsername()));
    }
}
