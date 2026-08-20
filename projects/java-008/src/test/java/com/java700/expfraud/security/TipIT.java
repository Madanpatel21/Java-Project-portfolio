package com.java700.expfraud.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.expfraud.common.TestDb;
import com.java700.expfraud.common.TestFixtures;
import java.time.Clock;
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

/** Anonymous whistleblower channel: open intake, restricted access, no identity capture. */
@SpringBootTest(classes = com.java700.expfraud.ExpenseFraudApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TipIT {

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

    private String employee;
    private String investigator;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "employee", Roles.EMPLOYEE, null);
        TestFixtures.createUser(localUsers, encoder, clock, "investigator",
                Roles.FRAUD_INVESTIGATOR, null);
        employee = TestFixtures.token(mvc, mapper, "employee");
        investigator = TestFixtures.token(mvc, mapper, "investigator");
    }

    @Test
    void anonymousTipIsAcceptedWithoutAuthentication() throws Exception {
        String body = mvc.perform(post("/api/v1/tips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"channel\":\"ANONYMOUS_WEB\",\"subject\":\"Fake mileage\","
                                + "\"description\":\"Employee inflates weekend mileage every month\","
                                + "\"relatedClaimNo\":\"EF-2026-00001\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode tip = mapper.readTree(body);
        assertThat(tip.get("tipNo").asText()).startsWith("TIP-2026-");
        assertThat(tip.get("status").asText()).isEqualTo("NEW");
        assertThat(tip.has("submitter")).isFalse();

        // Employees cannot list tips.
        mvc.perform(get("/api/v1/tips").header("Authorization", "Bearer " + employee))
                .andExpect(status().isForbidden());

        // Investigators can list and review.
        String list = mvc.perform(get("/api/v1/tips")
                        .header("Authorization", "Bearer " + investigator))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode tips = mapper.readTree(list);
        assertThat(tips).hasSize(1);
        assertThat(tips.get(0).get("subject").asText()).isEqualTo("Fake mileage");

        String tipId = tips.get(0).get("id").asText();
        mvc.perform(post("/api/v1/tips/" + tipId + "/review")
                        .header("Authorization", "Bearer " + investigator)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"outcome\":\"Linked to open fraud case\"}"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    JsonNode reviewed = mapper.readTree(result.getResponse().getContentAsString());
                    assertThat(reviewed.get("status").asText()).isEqualTo("CLOSED");
                });

        // Double review is rejected.
        mvc.perform(post("/api/v1/tips/" + tipId + "/review")
                        .header("Authorization", "Bearer " + investigator)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"outcome\":\"again\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidTipPayloadIsRejected() throws Exception {
        mvc.perform(post("/api/v1/tips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"channel\":\"CARRIER_PIGEON\",\"subject\":\"\",\"description\":\"x\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/tips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"channel\":\"ANONYMOUS_WEB\",\"subject\":\"ok\",\"description\":\" \"}"))
                .andExpect(status().isBadRequest());
    }
}
