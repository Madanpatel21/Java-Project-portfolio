package com.java700.crvs.certificates;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class CertificateApi {

    private CertificateApi() {
    }

    public record IssueRequest(@NotBlank String personId, @NotBlank String type) {
    }

    public record CertificateView(String id, String personId, String type, String token,
                                  String contentHash, String status, Instant issuedAt,
                                  String issuedBy, Instant revokedAt, String revokeReason) {

        static CertificateView from(Certificate c) {
            return new CertificateView(c.getId(), c.getPersonId(), c.getType().name(), c.getToken(),
                    c.getContentHash(), c.getStatus().name(), c.getIssuedAt(), c.getIssuedBy(),
                    c.getRevokedAt(), c.getRevokeReason());
        }
    }

    public record VerificationView(String token, boolean valid, String status, String type,
                                   String personName, String personDob, String personNationalId) {
    }

    public record RevokeRequest(@Size(max = 500) String reason) {
    }
}
