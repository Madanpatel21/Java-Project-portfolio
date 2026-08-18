package com.java700.workforce.policy;



import com.java700.workforce.common.api.PageResponse;
import com.java700.workforce.common.api.Problems;
import com.java700.workforce.common.audit.AuditLogService;
import com.java700.workforce.security.SecurityUtil;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Governs policy versioning: every version is immutable; activation is an explicit, audited step. */
@Service
public class PolicyService {

    public static final String CACHE_ACTIVE_RULES = "activePolicyRules";

    private final PolicyRepository policyRepository;
    private final PolicyVersionRepository versionRepository;
    private final AuditLogService audit;
    private final Clock clock;

    public PolicyService(PolicyRepository policyRepository, PolicyVersionRepository versionRepository,
                         AuditLogService audit, Clock clock) {
        this.policyRepository = policyRepository;
        this.versionRepository = versionRepository;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<PolicyApi.PolicyView> list(int page, int size) {
        return PageResponse.from(policyRepository.findAll(
                PageRequest.of(page, size, Sort.by("code"))).map(PolicyApi.PolicyView::from));
    }

    @Transactional(readOnly = true)
    public List<PolicyApi.VersionView> versions(String code) {
        Policy policy = policyRepository.findByCode(code)
                .orElseThrow(() -> new Problems.NotFound("Policy not found"));
        return versionRepository.findByPolicyIdOrderByVersionNoDesc(policy.getId()).stream()
                .map(PolicyApi.VersionView::from)
                .toList();
    }

    /** Creates a new (inactive) version and atomically activates it. */
    @Transactional
    @CacheEvict(cacheNames = CACHE_ACTIVE_RULES, allEntries = true)
    public PolicyApi.VersionView createVersion(String code, String rulesJson) {
        Policy policy = policyRepository.findByCode(code)
                .orElseThrow(() -> new Problems.NotFound("Policy not found"));
        int next = versionRepository.findByPolicyIdOrderByVersionNoDesc(policy.getId()).stream()
                .findFirst().map(PolicyVersion::getVersionNo).orElse(0) + 1;
        versionRepository.findByPolicyIdAndStatus(policy.getId(), "ACTIVE").ifPresent(active ->
                versionRepository.save(new PolicyVersion(active.getId(), active.getPolicyId(),
                        active.getVersionNo(), active.getRulesJson(), "SUPERSEDED",
                        active.getEffectiveFrom(), active.getCreatedBy(), active.getCreatedAt())));
        PolicyVersion version = new PolicyVersion(UUID.randomUUID().toString(), policy.getId(), next,
                rulesJson, "ACTIVE", Instant.now(clock), SecurityUtil.currentUsername(),
                Instant.now(clock));
        versionRepository.save(version);
        policy.activateVersion(version.getId());
        policyRepository.save(policy);
        audit.record("POLICY_VERSION_ACTIVATED", "POLICY", policy.getCode(),
                "Activated version " + next);
        return PolicyApi.VersionView.from(version);
    }

    /** Active rules JSON of the given policy, cached until the next version activation. */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_ACTIVE_RULES, key = "#code")
    public String activeRules(String code) {
        Policy policy = policyRepository.findByCode(code)
                .orElseThrow(() -> new Problems.NotFound("Policy not found"));
        return versionRepository.findById(policy.getActiveVersionId())
                .map(PolicyVersion::getRulesJson)
                .orElseThrow(() -> new Problems.NotFound("Policy has no active version"));
    }
}
