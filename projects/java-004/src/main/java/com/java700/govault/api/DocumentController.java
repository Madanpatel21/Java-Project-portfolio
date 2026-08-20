package com.java700.govault.api;

import com.java700.govault.common.api.PageResponse;
import com.java700.govault.service.Api;
import com.java700.govault.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "govault", description = "Document governance: quarantine, classification, retention, holds, disposition")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document (enters QUARANTINED until classified)")
    @PreAuthorize("isAuthenticated()")
    public Api.DocumentView upload(@RequestPart("file") MultipartFile file,
                                   @RequestParam(required = false) String title,
                                   @RequestHeader(value = "Idempotency-Key") String idemKey) {
        return service.upload(file, title, idemKey);
    }

    @GetMapping("/documents")
    @Operation(summary = "List documents (optional status filter)")
    @PreAuthorize("isAuthenticated()")
    public PageResponse<Api.DocumentView> documents(@RequestParam(required = false) String status,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return service.documents(status, page, size);
    }

    @GetMapping("/documents/search")
    @Operation(summary = "Full-text search over titles and extracted content")
    @PreAuthorize("isAuthenticated()")
    public PageResponse<Api.DocumentView> search(@RequestParam String q,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return service.search(q, page, size);
    }

    @PostMapping("/documents/{id}/classify")
    @Operation(summary = "Classify a document and assign its retention class (releases from quarantine)")
    @PreAuthorize("hasAnyRole('RECORDS_MANAGER','LEGAL_COUNSEL','ADMIN')")
    public Api.DocumentView classify(@PathVariable String id,
                                     @RequestHeader(value = "Idempotency-Key") String idemKey,
                                     @Valid @RequestBody Api.ClassifyRequest body) {
        return service.classify(id, body.classification(), body.retentionClass(), idemKey);
    }

    @GetMapping("/documents/{id}/download")
    @Operation(summary = "Download content (classification clearance enforced)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> download(@PathVariable String id) {
        byte[] content = service.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + id + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    @GetMapping("/documents/{id}/disposition-proofs")
    @Operation(summary = "Disposition proofs for a document (append-only)")
    @PreAuthorize("hasAnyRole('RECORDS_MANAGER','LEGAL_COUNSEL','AUDITOR','ADMIN')")
    public List<Api.ProofView> proofs(@PathVariable String id) {
        return service.dispositionProofs(id);
    }

    // ---- legal holds ----

    @PostMapping("/holds")
    @Operation(summary = "Create a legal hold")
    @PreAuthorize("hasAnyRole('LEGAL_COUNSEL','ADMIN')")
    public Api.HoldView createHold(@RequestHeader(value = "Idempotency-Key") String idemKey,
                                   @Valid @RequestBody Api.CreateHoldRequest body) {
        return service.createHold(body.name(), body.reason(), idemKey);
    }

    @GetMapping("/holds")
    @Operation(summary = "List legal holds")
    @PreAuthorize("hasAnyRole('LEGAL_COUNSEL','RECORDS_MANAGER','AUDITOR','ADMIN')")
    public List<Api.HoldView> holds() {
        return service.holds();
    }

    @PostMapping("/holds/{holdId}/apply/{documentId}")
    @Operation(summary = "Place a document under a legal hold")
    @PreAuthorize("hasAnyRole('LEGAL_COUNSEL','ADMIN')")
    public Api.HoldView apply(@PathVariable String holdId, @PathVariable String documentId) {
        return service.applyToDocument(holdId, documentId);
    }

    @PostMapping("/holds/{holdId}/release")
    @Operation(summary = "Release a legal hold")
    @PreAuthorize("hasAnyRole('LEGAL_COUNSEL','ADMIN')")
    public Api.HoldView release(@PathVariable String holdId) {
        return service.release(holdId);
    }

    // ---- retention ----

    @PostMapping("/retention/scan")
    @Operation(summary = "Run the retention scan manually (disposition + hold protection)")
    @PreAuthorize("hasAnyRole('RECORDS_MANAGER','ADMIN')")
    public Api.ScanResult scan() {
        return service.scan();
    }
}
