package com.java700.fleetmaint.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.fleetmaint.common.TestDb;
import com.java700.fleetmaint.common.TestFixtures;
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

/** Odometer tamper detection: rollback rejection, suspicious jumps, role boundaries. */
@SpringBootTest(classes = com.java700.fleetmaint.FleetMaintenanceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OdometerIT {

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
    private String driver;
    private String auditor;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "fleet", Roles.FLEET_MANAGER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "driver", Roles.DRIVER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "auditor", Roles.AUDITOR, null);
        fleet = TestFixtures.token(mvc, mapper, "fleet");
        driver = TestFixtures.token(mvc, mapper, "driver");
        auditor = TestFixtures.token(mvc, mapper, "auditor");
    }

    private String registerVehicle(int odometer) throws Exception {
        String body = String.format(
                "{\"vin\":\"VIN-TMP\",\"plate\":\"TMP-001\",\"make\":\"Ford\",\"model\":\"Transit\","
                        + "\"modelYear\":2021,\"category\":\"VAN\",\"initialOdometer\":%d,"
                        + "\"serviceAnchorOdometer\":%d,\"department\":\"FIELD\"}",
                odometer, odometer);
        String response = mvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + fleet)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("id").asText();
    }

    @Test
    void rollbackReadingsAreRejected() throws Exception {
        String vehicleId = registerVehicle(50000);
        mvc.perform(post("/api/v1/vehicles/" + vehicleId + "/odometer")
                        .header("Authorization", "Bearer " + driver)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reading\":49999,\"source\":\"DRIVER\"}"))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/v1/vehicles/" + vehicleId)
                        .header("Authorization", "Bearer " + fleet))
                .andExpect(jsonPath("$.currentOdometer").value(50000));
    }

    @Test
    void implausibleJumpIsAcceptedButFlagged() throws Exception {
        String vehicleId = registerVehicle(50000);
        String body = mvc.perform(post("/api/v1/vehicles/" + vehicleId + "/odometer")
                        .header("Authorization", "Bearer " + driver)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reading\":60000,\"source\":\"DRIVER\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode result = mapper.readTree(body);
        assertThat(result.get("accepted").asBoolean()).isTrue();
        assertThat(result.get("flag").asText()).isEqualTo("SUSPICIOUS_JUMP");

        String history = mvc.perform(get("/api/v1/vehicles/" + vehicleId + "/odometer")
                        .header("Authorization", "Bearer " + auditor))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(mapper.readTree(history).get(0).get("flag").asText())
                .isEqualTo("SUSPICIOUS_JUMP");
    }

    @Test
    void normalReadingIsAcceptedWithoutFlag() throws Exception {
        String vehicleId = registerVehicle(50000);
        mvc.perform(post("/api/v1/vehicles/" + vehicleId + "/odometer")
                        .header("Authorization", "Bearer " + driver)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reading\":50200,\"source\":\"DRIVER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value("OK"));
    }

    @Test
    void driverCannotRegisterVehiclesOrReadOdometerHistory() throws Exception {
        mvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + driver)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vin\":\"X\",\"plate\":\"X\",\"make\":\"X\",\"model\":\"X\","
                                + "\"modelYear\":2020,\"category\":\"VAN\",\"initialOdometer\":1}"))
                .andExpect(status().isForbidden());
        String vehicleId = registerVehicle(1000);
        mvc.perform(get("/api/v1/vehicles/" + vehicleId + "/odometer")
                        .header("Authorization", "Bearer " + driver))
                .andExpect(status().isForbidden());
    }
}
