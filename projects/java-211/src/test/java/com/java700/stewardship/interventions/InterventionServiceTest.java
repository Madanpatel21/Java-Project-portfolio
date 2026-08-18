package com.java700.stewardship.interventions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.stewardship.common.api.Problems;
import com.java700.stewardship.common.audit.AuditLogService;
import com.java700.stewardship.common.web.IdempotencyService;
import com.java700.stewardship.messaging.DomainEventBus;
import com.java700.stewardship.observability.Metrics;
import com.java700.stewardship.prescriptions.Prescription;
import com.java700.stewardship.prescriptions.PrescriptionRepository;
import com.java700.stewardship.prescriptions.PrescriptionService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InterventionServiceTest {

    private InterventionRepository repository;
    private PrescriptionRepository prescriptionRepository;
    private PrescriptionService prescriptionService;
    private IdempotencyService idempotency;
    private InterventionService service;
    private Prescription rx;

    @BeforeEach
    void setUp() {
        repository = mock(InterventionRepository.class);
        prescriptionRepository = mock(PrescriptionRepository.class);
        prescriptionService = mock(PrescriptionService.class);
        idempotency = mock(IdempotencyService.class);
        rx = new Prescription("rx1", "p1", "a1", "drug1", "SEPSIS", "IV",
                new BigDecimal("4500"), 8, Instant.now(), false, "dr", null, Instant.now());
        rx.activate();
        when(prescriptionRepository.findById("rx1")).thenReturn(Optional.of(rx));
        service = new InterventionService(repository, prescriptionRepository, prescriptionService,
                mock(DomainEventBus.class), idempotency, mock(AuditLogService.class),
                mock(Metrics.class), new ObjectMapper(), Clock.systemUTC());
    }

    private InterventionApi.ProposeRequest req() {
        return new InterventionApi.ProposeRequest("rx1", null, "IV_TO_PO",
                Map.of("doseMg", "1000", "frequencyHours", "12"),
                "Afebrile 72h, oral switch appropriate");
    }

    @Test
    void proposePersistsWithProposedStatus() {
        when(idempotency.begin("k1", "INTERVENTION")).thenReturn(null);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var view = service.propose(req(), "k1");
        assertThat(view.status()).isEqualTo("PROPOSED");
        assertThat(view.type()).isEqualTo("IV_TO_PO");
    }

    @Test
    void rejectRequiresClinicalReason() {
        Intervention i = new Intervention("i1", "rx1", null, "STOP", "{}", "reason",
                "pharmacist", Instant.now());
        when(repository.findById("i1")).thenReturn(Optional.of(i));
        assertThatThrownBy(() -> service.decide("i1", false, null))
                .isInstanceOf(Problems.BadRequest.class);
        assertThatThrownBy(() -> service.decide("i1", false, "   "))
                .isInstanceOf(Problems.BadRequest.class);
    }

    @Test
    void acceptAppliesTherapyChange() {
        Intervention i = new Intervention("i1", "rx1", null, "IV_TO_PO",
                "{\"doseMg\":\"1000\",\"frequencyHours\":\"12\"}", "switch",
                "pharmacist", Instant.now());
        when(repository.findById("i1")).thenReturn(Optional.of(i));
        var view = service.decide("i1", true, "agreed");
        assertThat(view.status()).isEqualTo("ACCEPTED");
        verify(prescriptionService).applyIntervention(eq("rx1"), eq("IV_TO_PO"), any());
    }

    @Test
    void doubleDecisionIsRejected() {
        Intervention i = new Intervention("i1", "rx1", null, "STOP", "{}", "reason",
                "pharmacist", Instant.now());
        i.decide(Intervention.Status.REJECTED, "prescriber", Instant.now(), "not indicated");
        when(repository.findById("i1")).thenReturn(Optional.of(i));
        assertThatThrownBy(() -> service.decide("i1", true, null))
                .isInstanceOf(Problems.Conflict.class);
    }

    @Test
    void unknownInterventionTypeRejected() {
        when(idempotency.begin("k2", "INTERVENTION")).thenReturn(null);
        assertThatThrownBy(() -> service.propose(
                new InterventionApi.ProposeRequest("rx1", null, "MAGIC_WAND",
                        Map.of(), "because"), "k2"))
                .isInstanceOf(Problems.BadRequest.class);
        verify(idempotency).abandon("k2");
        verify(repository, never()).save(any());
    }
}
