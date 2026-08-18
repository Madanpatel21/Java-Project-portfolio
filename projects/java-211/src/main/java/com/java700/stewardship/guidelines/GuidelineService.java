package com.java700.stewardship.guidelines;

import com.java700.stewardship.common.api.Problems;
import com.java700.stewardship.common.audit.AuditLogService;
import com.java700.stewardship.security.SecurityUtil;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Governs guideline versioning: versions are immutable; creating a new version supersedes
 * the active one. The active rule set is cached and invalidated on activation.
 */
@Service
public class GuidelineService {

    public static final String CACHE_ACTIVE_GUIDELINE = "activeGuideline";

    private final GuidelineRepository repository;
    private final AuditLogService audit;
    private final Clock clock;

    public GuidelineService(GuidelineRepository repository, AuditLogService audit, Clock clock) {
        this.repository = repository;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_ACTIVE_GUIDELINE)
    public StewardshipGuideline active() {
        return repository.findByStatus("ACTIVE")
                .orElseThrow(() -> new Problems.NotFound("No active guideline set"));
    }

    @Transactional(readOnly = true)
    public List<GuidelineApi.GuidelineView> versions() {
        return repository.findAllByOrderByVersionNoDesc().stream()
                .map(GuidelineApi.GuidelineView::from).toList();
    }

    @Transactional
    @CacheEvict(cacheNames = CACHE_ACTIVE_GUIDELINE, allEntries = true)
    public GuidelineApi.GuidelineView createVersion(String rulesJson) {
        StewardshipGuideline active = repository.findByStatus("ACTIVE").orElse(null);
        int next = active == null ? 1 : active.getVersionNo() + 1;
        if (active != null) {
            repository.save(new StewardshipGuideline(active.getId(), active.getName(),
                    active.getVersionNo(), "SUPERSEDED", active.getEffectiveFrom(),
                    active.getCreatedBy(), active.getRulesJson()));
        }
        StewardshipGuideline version = new StewardshipGuideline(UUID.randomUUID().toString(),
                "Hospital Stewardship Guideline Set", next, "ACTIVE", Instant.now(clock),
                SecurityUtil.currentUsername(), rulesJson);
        repository.save(version);
        audit.record("GUIDELINE_VERSION_ACTIVATED", "GUIDELINE", version.getId(),
                "Activated version " + next);
        return GuidelineApi.GuidelineView.from(version);
    }
}
