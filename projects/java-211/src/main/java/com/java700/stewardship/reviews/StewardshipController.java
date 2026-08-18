package com.java700.stewardship.reviews;

import com.java700.stewardship.guidelines.StewardshipFinding;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stewardship")
@Tag(name = "stewardship", description = "Rule-engine evaluation workbench")
public class StewardshipController {

    private final StewardshipEvaluation evaluation;

    public StewardshipController(StewardshipEvaluation evaluation) {
        this.evaluation = evaluation;
    }

    @GetMapping("/evaluate/{prescriptionId}")
    @Operation(summary = "Evaluate a prescription against guidelines, labs and culture evidence")
    @PreAuthorize("hasAnyRole('PHARMACIST','ID_PHYSICIAN','STEWARDSHIP_ADMIN')")
    public List<StewardshipFinding> evaluate(@PathVariable String prescriptionId) {
        return evaluation.evaluate(prescriptionId);
    }
}
