package com.java700.stewardship.reviews;

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
@RequestMapping("/api/v1/reviews")
@Tag(name = "reviews", description = "Stewardship review task queue")
public class ReviewController {

    private final ReviewTaskService service;

    public ReviewController(ReviewTaskService service) {
        this.service = service;
    }

    @GetMapping("/open")
    @Operation(summary = "List open review tasks (due first)")
    @PreAuthorize("hasAnyRole('PHARMACIST','ID_PHYSICIAN','STEWARDSHIP_ADMIN')")
    public List<ReviewApi.TaskView> open() {
        return service.openTasks();
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign a review task to a pharmacist")
    @PreAuthorize("hasAnyRole('PHARMACIST','STEWARDSHIP_ADMIN')")
    public ReviewApi.TaskView assign(@PathVariable String id, @Valid @RequestBody ReviewApi.AssignRequest body) {
        return service.assign(id, body.pharmacist());
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete a review task")
    @PreAuthorize("hasAnyRole('PHARMACIST','ID_PHYSICIAN','STEWARDSHIP_ADMIN')")
    public ReviewApi.TaskView complete(@PathVariable String id) {
        return service.complete(id);
    }
}
