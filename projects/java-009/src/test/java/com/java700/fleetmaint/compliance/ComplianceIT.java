package com.java700.fleetmaint.compliance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.fleetmaint.common.TestDb;
import com.java700.fleetmaint.common.TestFixtures;
import com.java700.fleetmaint.security.LocalUserService;
import com.java700.fleetmaint.security.Roles;
import java.time.Clock;
import java.time.LocalDate;
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

/** Compliance inspection ledger: holds on FAIL, release on PASS, compliance report. */
@SpringBootTest(classes = com.java700.fleetmaint.FleetMaintenanceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ComplianceIT {

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

    private String fleet;
    private String compliance;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "fleet", Roles.FLEET_MANAGER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "compliance",
                Roles.COMPLIANCE_OFFICER, null);
        fleet = TestFixtures.token(mvc, mapper, "fleet");
        compliance = TestFixtures.token(mvc, mapper, "compliance");
    }

    private String registerTruck() throws Exception {
        String body = String.format(
                "{\"vin\":\"VIN-COMP\",\"plate\":\"COMP-01\",\"make\":\"MAN\",\"model\":\"TGX\","
                        + "\"modelYear\":2022,\"category\":\"TRUCK\",\"initialOdometer\":80000,"
                        + "\"serviceAnchorOdometer\":80000,\"lastServiceDate\":\"%s\"}",
                LocalDate.now(clock).minusDays(400));
        String response = mvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + fleet)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("id").asText();
    }

    @Test
    void failedInspectionPlacesVehicleOnComplianceHoldAndPassReleasesIt() throws Exception {
        String vehicleId = registerTruck();
        mvc.perform(post("/api/v1/inspections/" + vehicleId)
                        .header("Authorization", "Bearer " + compliance)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inspectionType\":\"DOT_ANNUAL\",\"inspector\":\"R. Shah\","
                                + "\"result\":\"FAIL\",\"notes\":\"brake imbalance\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/vehicles/" + vehicleId)
                        .header("Authorization", "Bearer " + fleet))
                .andExpect(jsonPath("$.status").value("COMPLIANCE_HOLD"));

        String report = mvc.perform(get("/api/v1/inspections/compliance-report")
                        .header("Authorization", "Bearer " + compliance))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode rows = mapper.readTree(report);
        assertThat(rows).isNotEmpty();
        JsonNode dot = null;
        for (JsonNode row : rows) {
            if ("DOT-ANNUAL".equals(row.get("planCode").asText())) {
                dot = row;
            }
        }
        assertThat(dot).isNotNull();
        assertThat(dot.get("compliant").asBoolean()).isFalse();

        mvc.perform(post("/api/v1/inspections/" + vehicleId)
                        .header("Authorization", "Bearer " + compliance)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inspectionType\":\"DOT_ANNUAL\",\"inspector\":\"R. Shah\","
                                + "\"result\":\"PASS\",\"notes\":\"brakes replaced\","
                                + "\"validUntil\":\"" + LocalDate.now(clock).plusYears(1) + "\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/vehicles/" + vehicleId)
                        .header("Authorization", "Bearer " + fleet))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        String after = mvc.perform(get("/api/v1/inspections/compliance-report")
                        .header("Authorization", "Bearer " + compliance))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode dotAfter = null;
        for (JsonNode row : mapper.readTree(after)) {
            if ("DOT-ANNUAL".equals(row.get("planCode").asText())) {
                dotAfter = row;
            }
        }
        assertThat(dotAfter).isNotNull();
        assertThat(dotAfter.get("compliant").asBoolean()).isTrue();
    }

    @Test
    void overdueComplianceInspectionIsForecastedWithPriority() throws Exception {
        String vehicleId = registerTruck();
        mvc.perform(post("/api/v1/scheduling/forecast/run")
                        .header("Authorization", "Bearer " + fleet))
                .andExpect(status().isOk());
        String tasks = mvc.perform(get("/api/v1/scheduling/tasks")
                        .header("Authorization", "Bearer " + fleet))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode dot = null;
        for (JsonNode task : mapper.readTree(tasks)) {
            if ("DOT-ANNUAL".equals(task.get("planCode").asText())
                    && vehicleId.equals(task.get("vehicleId").asText())) {
                dot = task;
            }
        }
        assertThat(dot).isNotNull();
        assertThat(dot.get("status").asText()).isEqualTo("OVERDUE");
        assertThat(dot.get("priority").asText()).isEqualTo("COMPLIANCE");
    }

    @Test
    void fleetManagerCannotRecordInspections() throws Exception {
        String vehicleId = registerTruck();
        mvc.perform(post("/api/v1/inspections/" + vehicleId)
                        .header("Authorization", "Bearer " + fleet)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inspectionType\":\"DOT_ANNUAL\",\"inspector\":\"x\","
                                + "\"result\":\"PASS\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidInspectionResultIsRejected() throws Exception {
        String vehicleId = registerTruck();
        mvc.perform(post("/api/v1/inspections/" + vehicleId)
                        .header("Authorization", "Bearer " + compliance)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inspectionType\":\"DOT_ANNUAL\",\"inspector\":\"x\","
                                + "\"result\":\"MAYBE\"}"))
                .andExpect(status().isBadRequest());
    }
}
