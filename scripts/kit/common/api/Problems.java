package com.java700.kit.common.api;

/** Domain exception hierarchy mapped to RFC 7807 problem responses. */
public final class Problems {

    private Problems() {
    }

    public static class NotFound extends RuntimeException {
        public NotFound(String message) {
            super(message);
        }
    }

    public static class Conflict extends RuntimeException {
        public Conflict(String message) {
            super(message);
        }
    }

    public static class BadRequest extends RuntimeException {
        public BadRequest(String message) {
            super(message);
        }
    }

    public static class RateLimited extends RuntimeException {
        public RateLimited(String message) {
            super(message);
        }
    }

    public static class ServiceUnavailable extends RuntimeException {
        public ServiceUnavailable(String message) {
            super(message);
        }
    }
}
