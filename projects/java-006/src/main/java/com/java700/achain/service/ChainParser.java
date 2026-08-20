package com.java700.achain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Component;

/** Parses/validates approval-chain step definitions. */
@Component
public class ChainParser {

    private final ObjectMapper mapper;

    public ChainParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public record Step(int step, String role, int approversRequired) {
    }

    public List<Step> parse(String stepsJson) {
        try {
            List<Step> steps = mapper.readValue(stepsJson, new TypeReference<List<Step>>() {
            });
            if (steps.isEmpty()) {
                throw new IllegalArgumentException("Chain must have at least one step");
            }
            for (int i = 0; i < steps.size(); i++) {
                if (steps.get(i).step() != i + 1) {
                    throw new IllegalArgumentException("Steps must be numbered 1..N in order");
                }
                if (steps.get(i).approversRequired() < 1) {
                    throw new IllegalArgumentException("approversRequired must be >= 1");
                }
            }
            return steps;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed chain steps JSON", e);
        }
    }
}
