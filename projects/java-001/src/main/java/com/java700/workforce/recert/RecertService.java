package com.java700.workforce.recert;




import com.java700.workforce.access.GrantRepository;
import com.java700.workforce.common.api.Problems;
import com.java700.workforce.common.audit.AuditLogService;
import com.java700.workforce.evidence.EvidenceService;
import com.java700.workforce.messaging.DomainEvent;
import com.java700.workforce.messaging.DomainEventBus;
import com.java700.workforce.security.SecurityUtil;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Recertification campaigns: generate reviews, record keep/revoke decisions, enforce outcomes. */
@Service
public class RecertService {

    private final RecertCampaignRepository campaignRepository;
    private final RecertDecisionRepository decisionRepository;
    private final GrantRepository grantRepository;
    private final EvidenceService evidence;
    private final AuditLogService audit;
    private final DomainEventBus bus;
    private final Clock clock;

    public RecertService(RecertCampaignRepository campaignRepository,
                         RecertDecisionRepository decisionRepository, GrantRepository grantRepository,
                         EvidenceService evidence, AuditLogService audit, DomainEventBus bus, Clock clock) {
        this.campaignRepository = campaignRepository;
        this.decisionRepository = decisionRepository;
        this.grantRepository = grantRepository;
        this.evidence = evidence;
        this.audit = audit;
        this.bus = bus;
        this.clock = clock;
    }

    @Transactional
    public RecertApi.CampaignView generateCampaign(String name, int windowDays) {
        Instant now = Instant.now(clock);
        RecertCampaign campaign = new RecertCampaign(UUID.randomUUID().toString(), name, now,
                now.plus(Math.max(1, windowDays), ChronoUnit.DAYS), SecurityUtil.currentUsername(), now);
        campaignRepository.save(campaign);
        audit.record("RECERT_CAMPAIGN_GENERATED", "RECERT_CAMPAIGN", campaign.getId(),
                "Campaign " + name + " (" + windowDays + "d window)");
        return RecertApi.CampaignView.from(campaign);
    }

    @Transactional
    public RecertApi.DecisionView decide(String campaignId, String grantId, String decision) {
        RecertCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new Problems.NotFound("Campaign not found"));
        if (!"OPEN".equals(campaign.getStatus())) {
            throw new Problems.Conflict("Campaign is closed");
        }
        var grant = grantRepository.findByIdAndStatus(grantId, "ACTIVE")
                .orElseThrow(() -> new Problems.NotFound("Active grant not found"));
        if (!"KEEP".equals(decision) && !"REVOKE".equals(decision)) {
            throw new Problems.BadRequest("Decision must be KEEP or REVOKE");
        }
        RecertDecision recert = new RecertDecision(UUID.randomUUID().toString(), campaignId, grantId,
                SecurityUtil.currentUsername(), decision, Instant.now(clock));
        decisionRepository.save(recert);
        if ("KEEP".equals(decision)) {
            grant.recertify(Instant.now(clock));
            grant.extendRecertDue(Instant.now(clock).plus(90, ChronoUnit.DAYS));
            grantRepository.save(grant);
            evidence.append("ACCESS_GRANT", grantId, "GRANT_RECERTIFIED", SecurityUtil.currentUsername(),
                    Map.of("campaignId", campaignId));
        } else {
            grant.revoke(Instant.now(clock), SecurityUtil.currentUsername(),
                    "Revoked by recertification campaign " + campaignId);
            grantRepository.save(grant);
            evidence.append("ACCESS_GRANT", grantId, "GRANT_REVOKED", SecurityUtil.currentUsername(),
                    Map.of("reason", "Recertification decision REVOKE", "campaignId", campaignId));
            bus.publish(new GrantRevokedByRecert(UUID.randomUUID().toString(), Instant.now(clock),
                    grantId, campaignId));
        }
        audit.record("RECERT_DECISION", "ACCESS_GRANT", grantId, decision + " in campaign " + campaignId);
        return RecertApi.DecisionView.from(recert);
    }

    @Transactional(readOnly = true)
    public List<RecertApi.CampaignView> campaigns() {
        return campaignRepository.findAll().stream().map(RecertApi.CampaignView::from).toList();
    }

    @Transactional(readOnly = true)
    public List<RecertApi.DecisionView> decisions(String campaignId) {
        return decisionRepository.findByCampaignId(campaignId).stream()
                .map(RecertApi.DecisionView::from).toList();
    }

    public record GrantRevokedByRecert(String eventId, Instant occurredAt, String grantId,
                                       String campaignId) implements DomainEvent {
    }
}
