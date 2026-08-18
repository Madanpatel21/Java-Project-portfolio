package com.java700.stewardship.reviews;

import com.java700.stewardship.common.api.Problems;
import com.java700.stewardship.common.audit.AuditLogService;
import com.java700.stewardship.observability.Metrics;
import com.java700.stewardship.prescriptions.PrescriptionRepository;
import com.java700.stewardship.security.SecurityUtil;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Review-task lifecycle with per-prescription trigger deduplication. */
@Service
public class ReviewTaskService {

    private final ReviewTaskRepository repository;
    private final PrescriptionRepository prescriptionRepository;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final Clock clock;

    public ReviewTaskService(ReviewTaskRepository repository,
                             PrescriptionRepository prescriptionRepository,
                             AuditLogService audit, Metrics metrics, Clock clock) {
        this.repository = repository;
        this.prescriptionRepository = prescriptionRepository;
        this.audit = audit;
        this.metrics = metrics;
        this.clock = clock;
    }

    /** Creates a task unless an OPEN one with the same trigger already exists. */
    @Transactional
    public ReviewTask createIfAbsent(String prescriptionId, String triggerReason, Instant dueAt) {
        var existing = repository.findByPrescriptionIdAndStatusAndTriggerReason(
                prescriptionId, "OPEN", triggerReason);
        if (existing.isPresent()) {
            return existing.get();
        }
        ReviewTask task = new ReviewTask(UUID.randomUUID().toString(), prescriptionId,
                triggerReason, dueAt, "system", Instant.now(clock));
        repository.save(task);
        metrics.incrementReviewTasks();
        audit.record("REVIEW_TASK_CREATED", "REVIEW_TASK", task.getId(),
                triggerReason + " review due for prescription " + prescriptionId);
        return task;
    }

    @Transactional
    public ReviewApi.TaskView assign(String taskId, String pharmacist) {
        ReviewTask task = load(taskId);
        if (task.getStatus() != ReviewTask.Status.OPEN) {
            throw new Problems.Conflict("Task is not open");
        }
        task.assign(pharmacist);
        repository.save(task);
        return ReviewApi.TaskView.from(task);
    }

    @Transactional
    public ReviewApi.TaskView complete(String taskId) {
        ReviewTask task = load(taskId);
        if (task.getStatus() != ReviewTask.Status.OPEN) {
            throw new Problems.Conflict("Task is not open");
        }
        task.complete(Instant.now(clock));
        repository.save(task);
        audit.record("REVIEW_TASK_COMPLETED", "REVIEW_TASK", taskId,
                "Completed by " + SecurityUtil.currentUsername());
        return ReviewApi.TaskView.from(task);
    }

    @Transactional
    public void cancelForPrescription(String prescriptionId) {
        for (ReviewTask task : repository.findByPrescriptionIdAndStatus(prescriptionId, "OPEN")) {
            task.cancel(Instant.now(clock));
            repository.save(task);
        }
    }

    @Transactional(readOnly = true)
    public List<ReviewApi.TaskView> openTasks() {
        return repository.findByStatusOrderByDueAtAsc("OPEN").stream()
                .map(ReviewApi.TaskView::from).toList();
    }

    private ReviewTask load(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Review task not found"));
    }
}
