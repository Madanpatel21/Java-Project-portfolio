package com.java700.fleetmaint.service;

import com.java700.fleetmaint.common.api.Problems;
import com.java700.fleetmaint.common.audit.AuditLogService;
import com.java700.fleetmaint.domain.MaintenancePlan;
import com.java700.fleetmaint.domain.MaintenancePlanRepository;
import com.java700.fleetmaint.domain.PlanItem;
import com.java700.fleetmaint.domain.PlanItemRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Maintenance plan catalogue with parts kits. */
@Service
public class PlanService {

    private static final Set<String> INTERVALS = Set.of(
            MaintenancePlan.INTERVAL_ODOMETER, MaintenancePlan.INTERVAL_CALENDAR);

    private final MaintenancePlanRepository plans;
    private final PlanItemRepository items;
    private final AuditLogService audit;
    private final Clock clock;

    public PlanService(MaintenancePlanRepository plans, PlanItemRepository items,
                       AuditLogService audit, Clock clock) {
        this.plans = plans;
        this.items = items;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    public MaintenancePlan create(Api.PlanRequest request) {
        if (request.code() == null || request.code().isBlank()) {
            throw new Problems.BadRequest("code is required");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new Problems.BadRequest("name is required");
        }
        if (!INTERVALS.contains(request.intervalType())) {
            throw new Problems.BadRequest("intervalType must be ODOMETER or CALENDAR");
        }
        if (request.intervalValue() <= 0) {
            throw new Problems.BadRequest("intervalValue must be positive");
        }
        if (plans.findByCode(request.code()).isPresent()) {
            throw new Problems.Conflict("plan code " + request.code() + " already exists");
        }
        MaintenancePlan plan = new MaintenancePlan(UUID.randomUUID().toString(),
                request.code().trim(), request.name().trim(), request.appliesToCategory(),
                request.intervalType(), request.intervalValue(), request.complianceRequired(),
                true, Instant.now(clock));
        MaintenancePlan saved = plans.save(plan);
        for (Api.PlanItemRequest item : request.items()) {
            if (item.quantity() <= 0) {
                throw new Problems.BadRequest("plan item quantity must be positive");
            }
            items.save(new PlanItem(UUID.randomUUID().toString(), saved.getId(),
                    item.partCode(), item.partName(), item.quantity(), item.estimatedCost()));
        }
        audit.record("PLAN_CREATED", "maintenance_plan", saved.getCode(),
                "interval=" + saved.getIntervalType() + "/" + saved.getIntervalValue());
        return saved;
    }

    @Transactional
    public MaintenancePlan setActive(String planId, boolean active) {
        MaintenancePlan plan = plans.findById(planId)
                .orElseThrow(() -> new Problems.NotFound("maintenance plan " + planId));
        plan.setActive(active);
        MaintenancePlan saved = plans.save(plan);
        audit.record("PLAN_TOGGLED", "maintenance_plan", saved.getCode(), "active=" + active);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<MaintenancePlan> all() {
        return plans.findAll();
    }

    @Transactional(readOnly = true)
    public Api.PlanView view(MaintenancePlan plan) {
        List<Api.PlanItemView> itemViews = new ArrayList<>();
        for (PlanItem item : items.findByPlanId(plan.getId())) {
            itemViews.add(new Api.PlanItemView(item.getPartCode(), item.getPartName(),
                    item.getQuantity(), item.getEstimatedCost()));
        }
        return new Api.PlanView(plan.getId(), plan.getCode(), plan.getName(),
                plan.getAppliesToCategory(), plan.getIntervalType(), plan.getIntervalValue(),
                plan.isComplianceRequired(), plan.isActive(), itemViews);
    }
}
