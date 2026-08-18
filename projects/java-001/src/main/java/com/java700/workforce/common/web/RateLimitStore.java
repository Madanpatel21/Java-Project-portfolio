package com.java700.workforce.common.web;

/** Backplane abstraction for rate-limit counters. */
public interface RateLimitStore {

    /** Increments the window for {@code key}; true if under {@code limit}, false if exceeded. */
    boolean allow(String key, int limit);
}
