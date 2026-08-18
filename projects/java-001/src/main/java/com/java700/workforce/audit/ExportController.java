package com.java700.workforce.audit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit/exports")
@Tag(name = "exports", description = "Auditor-grade signed evidence export bundles")
public class ExportController {

    private final ExportService service;

    public ExportController(ExportService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create an async export job (evidence bundle + HMAC signature)")
    @PreAuthorize("hasAnyRole('AUDITOR','COMPLIANCE_ADMIN','COMPLIANCE_OFFICER')")
    public ExportApi.ExportJobView create(@Valid @RequestBody ExportApi.CreateExportRequest body,
                                          @RequestHeader(value = "Idempotency-Key") String idempotencyKey) {
        return service.createExport(body.scopeUserId(), body.rangeFrom(), body.rangeTo(), idempotencyKey);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get export job status")
    @PreAuthorize("hasAnyRole('AUDITOR','COMPLIANCE_ADMIN','COMPLIANCE_OFFICER')")
    public ExportApi.ExportJobView get(@PathVariable String id) {
        return service.get(id);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download the JSONL evidence bundle")
    @PreAuthorize("hasAnyRole('AUDITOR','COMPLIANCE_ADMIN','COMPLIANCE_OFFICER')")
    public ResponseEntity<byte[]> download(@PathVariable String id) {
        byte[] data = service.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"evidence-" + id + ".jsonl\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @GetMapping("/{id}/verify")
    @Operation(summary = "Re-verify the bundle's HMAC signature server-side")
    @PreAuthorize("hasAnyRole('AUDITOR','COMPLIANCE_ADMIN','COMPLIANCE_OFFICER')")
    public ExportApi.VerifyResponse verify(@PathVariable String id) {
        return service.verify(id);
    }
}
