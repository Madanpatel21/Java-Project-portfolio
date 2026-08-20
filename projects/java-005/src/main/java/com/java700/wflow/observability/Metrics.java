package com.java700.wflow.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/** Typed business metrics for workflow orchestration. */
@Component
public class Metrics {

    private final Counter instancesStarted;
    private final Counter instancesCompleted;
    private final Counter tasksCreated;
    private final Counter tasksEscalated;
    private final AtomicLong pendingTasks = new AtomicLong();

    public Metrics(MeterRegistry registry) {
        this.instancesStarted = Counter.builder("wflow.instances.started")
                .description("Workflow instances started").register(registry);
        this.instancesCompleted = Counter.builder("wflow.instances.completed")
                .description("Workflow instances completed").register(registry);
        this.tasksCreated = Counter.builder("wflow.tasks.created")
                .description("Workflow tasks created").register(registry);
        this.tasksEscalated = Counter.builder("wflow.tasks.escalated")
                .description("Tasks escalated past SLA").register(registry);
        registry.gauge("wflow.tasks.pending", pendingTasks);
    }

    public void incrementInstancesStarted() {
        instancesStarted.increment();
    }

    public void incrementInstancesCompleted() {
        instancesCompleted.increment();
    }

    public void incrementTasksCreated() {
        tasksCreated.increment();
    }

    public void incrementTasksEscalated() {
        tasksEscalated.increment();
    }

    public void setPendingTasks(long n) {
        pendingTasks.set(n);
    }
}
