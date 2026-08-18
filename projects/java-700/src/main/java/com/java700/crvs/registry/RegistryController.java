package com.java700.crvs.registry;

import com.java700.crvs.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/persons")
@Tag(name = "persons", description = "Lifetime identity records (identity masked in listings)")
public class RegistryController {

    private final RegistryService service;

    public RegistryController(RegistryService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Search persons by name (masked views)")
    @PreAuthorize("hasAnyRole('REGISTRAR','SUPERVISOR','STATISTICIAN','ADMIN')")
    public PageResponse<RegistryApi.PersonView> search(@RequestParam(required = false) String query,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return service.search(query, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a person record")
    @PreAuthorize("hasAnyRole('REGISTRAR','SUPERVISOR','STATISTICIAN','ADMIN')")
    public RegistryApi.PersonView get(@PathVariable String id) {
        return RegistryApi.PersonView.from(service.get(id));
    }
}
