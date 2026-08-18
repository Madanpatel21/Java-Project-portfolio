package com.java700.stewardship.catalog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/drugs")
@Tag(name = "drugs", description = "Antimicrobial formulary catalog")
public class DrugController {

    private final DrugService service;

    public DrugController(DrugService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List the antimicrobial formulary")
    @PreAuthorize("isAuthenticated()")
    public List<DrugApi.DrugView> list() {
        return service.list();
    }
}
