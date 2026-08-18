package com.java700.stewardship.microbiology;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "microbiology", description = "Cultures, isolates and susceptibility reporting")
public class MicrobiologyController {

    private final MicrobiologyService service;

    public MicrobiologyController(MicrobiologyService service) {
        this.service = service;
    }

    @PostMapping("/cultures")
    @Operation(summary = "Create a culture")
    @PreAuthorize("hasAnyRole('MICROBIOLOGIST','ID_PHYSICIAN','STEWARDSHIP_ADMIN')")
    public String createCulture(@Valid @RequestBody MicrobiologyApi.CreateCultureRequest body) {
        return service.createCulture(body);
    }

    @PostMapping("/cultures/{id}/isolates")
    @Operation(summary = "Add an isolate with susceptibility results")
    @PreAuthorize("hasAnyRole('MICROBIOLOGIST','ID_PHYSICIAN')")
    public void addIsolate(@PathVariable String id, @Valid @RequestBody MicrobiologyApi.AddIsolateRequest body) {
        service.addIsolate(id, body);
    }

    @PostMapping("/cultures/{id}/report")
    @Operation(summary = "Report (finalize) a culture — triggers stewardship evaluation")
    @PreAuthorize("hasAnyRole('MICROBIOLOGIST','ID_PHYSICIAN')")
    public void report(@PathVariable String id) {
        service.report(id);
    }

    @GetMapping("/patients/{patientId}/cultures")
    @Operation(summary = "List cultures for a patient")
    @PreAuthorize("isAuthenticated()")
    public List<MicrobiologyApi.CultureView> cultures(@PathVariable String patientId) {
        return service.culturesFor(patientId);
    }
}
