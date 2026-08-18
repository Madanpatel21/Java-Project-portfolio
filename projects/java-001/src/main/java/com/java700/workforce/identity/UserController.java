package com.java700.workforce.identity;

import com.java700.workforce.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Validated
@Tag(name = "users", description = "Workforce user directory (PII masked in responses)")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user profile; employees may only read their own record")
    @PreAuthorize("isAuthenticated()")
    public UserApi.UserView get(@PathVariable String id) {
        return UserApi.UserView.from(service.get(id));
    }

    @GetMapping
    @Operation(summary = "Search users")
    @PreAuthorize("hasAnyRole('AUDITOR','COMPLIANCE_ADMIN','COMPLIANCE_OFFICER','ACCESS_MANAGER')")
    public PageResponse<UserApi.UserView> search(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.search(query, page, Math.min(size, 100));
    }
}
