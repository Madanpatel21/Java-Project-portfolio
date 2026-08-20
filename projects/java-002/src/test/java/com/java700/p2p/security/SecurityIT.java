package com.java700.p2p.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.p2p.common.TestDb;
import com.java700.p2p.common.TestFixtures;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Authorization matrix + attack-surface tests. */
@SpringBootTest(classes = com.java700.p2p.ProcureToPayApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIT {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    LocalUserService localUsers;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    Clock clock;

    private String procurement;
    private String clerk;
    private String manager;
    private String auditor;
    private String admin;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "procurement", Roles.PROCUREMENT, null);
        TestFixtures.createUser(localUsers, encoder, clock, "clerk", Roles.AP_CLERK, null);
        TestFixtures.createUser(localUsers, encoder, clock, "manager", Roles.AP_MANAGER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "auditor", Roles.AUDITOR, null);
        TestFixtures.createUser(localUsers, encoder, clock, "admin", Roles.ADMIN, null);
        procurement = TestFixtures.token(mvc, mapper, "procurement");
        clerk = TestFixtures.token(mvc, mapper, "clerk");
        manager = TestFixtures.token(mvc, mapper, "manager");
        auditor = TestFixtures.token(mvc, mapper, "auditor");
        admin = TestFixtures.token(mvc, mapper, "admin");
    }

    @Test
    void unauthenticatedAndForgedTokensRejected() throws Exception {
        mvc.perform(get("/api/v1/invoices")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/invoices").header("Authorization", "Bearer forged.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clerkCannotCreatePurchaseOrders() throws Exception {
        mvc.perform(post("/api/v1/po")
                        .header("Authorization", "Bearer " + clerk)
                        .header("Idempotency-Key", "sec-po-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "poNumber", "PO-SEC", "supplierId", "S", "supplierName", "S",
                                "currency", "USD", "lines", java.util.List.of(Map.of(
                                        "itemCode", "X", "description", "X", "quantity", 1,
                                        "unitPrice", 1))))))
                .andExpect(status().isForbidden());
    }

    @Test
    void clerkCannotWaiveExceptions() throws Exception {
        // covered end-to-end in P2PFlowIT; here the endpoint-level gate
        mvc.perform(post("/api/v1/exceptions/anything/waive")
                        .header("Authorization", "Bearer " + clerk)
                        .header("Idempotency-Key", "sec-waive-1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyAdminUpdatesToleranceRules() throws Exception {
        String ruleId = jdbc.queryForObject(
                "select id from tolerance_rules where rule_type = 'PRICE_VARIANCE'", String.class);
        mvc.perform(post("/api/v1/tolerance-rules/" + ruleId)
                        .header("Authorization", "Bearer " + manager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tolerancePct\":5.0,\"action\":\"WARN\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/tolerance-rules/" + ruleId)
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tolerancePct\":5.0,\"action\":\"WARN\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void invalidRuleActionRejected() throws Exception {
        String ruleId = jdbc.queryForObject(
                "select id from tolerance_rules where rule_type = 'PRICE_VARIANCE'", String.class);
        mvc.perform(post("/api/v1/tolerance-rules/" + ruleId)
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tolerancePct\":5.0,\"action\":\"EXPLODE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sqlInjectionAttemptsAreHarmless() throws Exception {
        mvc.perform(get("/api/v1/invoices").header("Authorization", "Bearer " + clerk)
                        .param("status", "NEW'; DROP TABLE invoices;--"))
                .andExpect(status().isOk());
    }

    @Test
    void lockoutAfterRepeatedFailures() throws Exception {
        String id = UUID.randomUUID().toString();
        localUsers.save(new LocalUser(id, "locktarget", encoder.encode(TestFixtures.PASSWORD),
                "locktarget@corp.example", null, Instant.now(clock)));
        localUsers.saveRole(id, Roles.AP_CLERK);
        for (int i = 0; i < 4; i++) {
            mvc.perform(post("/api/v1/auth/token").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"locktarget\",\"password\":\"wrong\"}"))
                    .andExpect(status().isNotFound());
        }
        mvc.perform(post("/api/v1/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"locktarget\",\"password\":\"wrong\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"locktarget\",\"password\":\""
                                + TestFixtures.PASSWORD + "\"}"))
                .andExpect(status().isTooManyRequests());
    }
}
