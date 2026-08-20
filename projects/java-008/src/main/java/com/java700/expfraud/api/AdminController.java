package com.java700.expfraud.api;

import com.java700.expfraud.security.SecurityUtil;
import com.java700.expfraud.service.AdminService;
import com.java700.expfraud.service.Api;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Administration: policy rules, peer baselines and platform statistics. */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService admin;

    public AdminController(AdminService admin) {
        this.admin = admin;
    }

    @GetMapping("/rules")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Api.RuleView> rules() {
        return admin.rules();
    }

    @PostMapping("/rules/{code}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public Api.RuleView setActive(@PathVariable String code, @RequestParam boolean active) {
        return admin.setActive(code, active, SecurityUtil.currentUsername());
    }

    @GetMapping("/baselines")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    public List<Api.BaselineView> baselines() {
        return admin.baselines();
    }

    @PostMapping("/baselines/recompute")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Api.BaselineView> recomputeBaselines() {
        return admin.recomputeBaselines();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    public Api.StatsView stats() {
        return admin.stats();
    }
}
