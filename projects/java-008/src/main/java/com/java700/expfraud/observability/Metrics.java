package com.java700.expfraud.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/** Typed business metrics for the fraud analytics engine. */
@Component
public class Metrics {

    private final Counter claimsSubmitted;
    private final Counter claimsScored;
    private final DistributionSummary riskScores;
    private final Counter casesOpened;
    private final Counter casesConfirmedFraud;
    private final Counter casesCleared;
    private final Counter tipsReceived;
    private final Counter duplicateGroupsCreated;
    private final Timer scoringTimer;

    public Metrics(MeterRegistry registry) {
        this.claimsSubmitted = registry.counter("expfraud.claims.submitted");
        this.claimsScored = registry.counter("expfraud.claims.scored");
        this.riskScores = registry.summary("expfraud.claims.risk_score");
        this.casesOpened = registry.counter("expfraud.cases.opened");
        this.casesConfirmedFraud = registry.counter("expfraud.cases.confirmed_fraud");
        this.casesCleared = registry.counter("expfraud.cases.cleared");
        this.tipsReceived = registry.counter("expfraud.tips.received");
        this.duplicateGroupsCreated = registry.counter("expfraud.duplicates.groups_created");
        this.scoringTimer = registry.timer("expfraud.scoring.duration");
    }

    public void claimSubmitted() {
        claimsSubmitted.increment();
    }

    public void claimScored(int score) {
        claimsScored.increment();
        riskScores.record(score);
    }

    public void caseOpened() {
        casesOpened.increment();
    }

    public void caseConfirmedFraud() {
        casesConfirmedFraud.increment();
    }

    public void caseCleared() {
        casesCleared.increment();
    }

    public void tipReceived() {
        tipsReceived.increment();
    }

    public void duplicateGroupCreated() {
        duplicateGroupsCreated.increment();
    }

    public Timer scoringTimer() {
        return scoringTimer;
    }
}
