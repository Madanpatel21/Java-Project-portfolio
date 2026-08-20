package com.java700.expfraud.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.expfraud.security.LocalUser;
import com.java700.expfraud.security.LocalUserService;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Shared test helpers: user creation, token issuance and claim submission. */
public final class TestFixtures {

    public static final String PASSWORD = "Password123!";

    private TestFixtures() {
    }

    public static String createUser(LocalUserService localUsers, PasswordEncoder encoder,
                                    Clock clock, String username, String role, String officeId) {
        String id = UUID.randomUUID().toString();
        localUsers.save(new LocalUser(id, username, encoder.encode(PASSWORD),
                username + "@corp.example", officeId, Instant.now(clock)));
        localUsers.saveRole(id, role);
        return id;
    }

    public static String token(MockMvc mvc, ObjectMapper mapper, String username) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(body);
        return node.get("accessToken").asText();
    }

    /** Submits an expense claim and expects HTTP 201; returns the claim view JSON. */
    public static JsonNode submitClaim(MockMvc mvc, ObjectMapper mapper, String token,
                                       String employeeId, String employeeName, String department,
                                       String category, String amount, String merchant,
                                       String expenseDate, String receiptRef, String idem)
            throws Exception {
        String body = String.format(
                "{\"employeeId\":\"%s\",\"employeeName\":\"%s\",\"department\":\"%s\","
                        + "\"category\":\"%s\",\"amount\":%s,\"currency\":\"USD\",\"merchant\":\"%s\","
                        + "\"expenseDate\":\"%s\",\"receiptRef\":\"%s\",\"description\":\"test\"}",
                employeeId, employeeName, department, category, amount, merchant, expenseDate,
                receiptRef);
        MockHttpServletRequestBuilder builder = post("/api/v1/claims")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body);
        if (idem != null) {
            builder = builder.header("Idempotency-Key", idem);
        }
        MvcResult result = mvc.perform(builder).andExpect(status().isCreated()).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString());
    }
}
