package com.java700.expfraud.api;

import com.java700.expfraud.domain.Tip;
import com.java700.expfraud.security.SecurityUtil;
import com.java700.expfraud.service.Api;
import com.java700.expfraud.service.TipService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Anonymous whistleblower channel. POST is open (no authentication); GET and review are
 * restricted to investigators, auditors and administrators.
 */
@RestController
@RequestMapping("/api/v1/tips")
public class TipController {

    private final TipService tips;

    public TipController(TipService tips) {
        this.tips = tips;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Api.TipView submit(@Valid @RequestBody Api.TipSubmitRequest request) {
        Tip tip = tips.submit(request);
        return tips.view(tip);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FRAUD_INVESTIGATOR','AUDITOR','ADMIN')")
    public List<Api.TipView> openTips() {
        return TipService.sortNewestFirst(tips.openTips(), tips);
    }

    @PostMapping("/{tipId}/review")
    @PreAuthorize("hasAnyRole('FRAUD_INVESTIGATOR','AUDITOR')")
    public Api.TipView review(@PathVariable String tipId,
                              @Valid @RequestBody Api.TipReviewRequest request) {
        Tip tip = tips.review(tipId, request.outcome(), SecurityUtil.currentUsername());
        return tips.view(tip);
    }
}
