package com.java700.crvs.verification;

public final class VerificationApi {

    private VerificationApi() {
    }

    public record PersonVerification(String nationalId, boolean exists, String status,
                                     String fullName, String dob, String region,
                                     String deceasedAt) {
    }
}
