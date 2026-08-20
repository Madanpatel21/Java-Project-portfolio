package com.java700.expfraud.service;

import com.java700.expfraud.common.api.Problems;
import com.java700.expfraud.common.audit.AuditLogService;
import com.java700.expfraud.domain.Tip;
import com.java700.expfraud.domain.TipRepository;
import com.java700.expfraud.observability.Metrics;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Anonymous whistleblower intake. Submissions never capture any submitter identity; the
 * caller receives only the tip number, which doubles as a follow-up tracking reference.
 */
@Service
public class TipService {

    private static final Set<String> CHANNELS = Set.of("ANONYMOUS_WEB", "SECURE_MAIL");

    private final TipRepository tips;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final Clock clock;

    public TipService(TipRepository tips, AuditLogService audit, Metrics metrics, Clock clock) {
        this.tips = tips;
        this.audit = audit;
        this.metrics = metrics;
        this.clock = clock;
    }

    @Transactional
    public Tip submit(Api.TipSubmitRequest request) {
        if (!CHANNELS.contains(request.channel())) {
            throw new Problems.BadRequest("channel must be one of " + CHANNELS);
        }
        if (request.subject() == null || request.subject().isBlank()) {
            throw new Problems.BadRequest("subject is required");
        }
        if (request.description() == null || request.description().isBlank()) {
            throw new Problems.BadRequest("description is required");
        }
        Tip tip = new Tip(UUID.randomUUID().toString(), nextTipNo(), request.channel(),
                request.subject().trim(), request.description().trim(),
                trimToNull(request.relatedClaimNo()), Tip.STATUS_NEW, Instant.now(clock));
        Tip saved = tips.save(tip);
        metrics.tipReceived();
        audit.record("TIP_SUBMITTED", "tip", saved.getTipNo(),
                "channel=" + saved.getChannel() + " (submitter identity not recorded)");
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Tip> openTips() {
        return tips.findByStatusOrderBySubmittedAtAsc(Tip.STATUS_NEW);
    }

    @Transactional(readOnly = true)
    public Tip load(String tipId) {
        return tips.findById(tipId).orElseThrow(() -> new Problems.NotFound("tip " + tipId));
    }

    @Transactional
    public Tip review(String tipId, String outcome, String username) {
        if (outcome == null || outcome.isBlank()) {
            throw new Problems.BadRequest("outcome is required");
        }
        Tip tip = load(tipId);
        if (!Tip.STATUS_NEW.equals(tip.getStatus())) {
            throw new Problems.Conflict("tip " + tip.getTipNo() + " is already " + tip.getStatus());
        }
        tip.review(username, outcome.trim(), Instant.now(clock));
        Tip saved = tips.save(tip);
        audit.record("TIP_REVIEWED", "tip", saved.getTipNo(),
                "by=" + username + " outcome=" + outcome);
        return saved;
    }

    public Api.TipView view(Tip tip) {
        return new Api.TipView(tip.getId(), tip.getTipNo(), tip.getChannel(), tip.getSubject(),
                tip.getDescription(), tip.getRelatedClaimNo(), tip.getStatus(), tip.getOutcome(),
                tip.getSubmittedAt(), tip.getReviewedAt());
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private synchronized String nextTipNo() {
        long count = tips.count();
        return "TIP-2026-" + String.format("%05d", count + 1);
    }

    public static List<Api.TipView> sortNewestFirst(List<Tip> tipList, TipService service) {
        return tipList.stream()
                .sorted(Comparator.comparing(Tip::getSubmittedAt).reversed())
                .map(service::view)
                .toList();
    }
}
