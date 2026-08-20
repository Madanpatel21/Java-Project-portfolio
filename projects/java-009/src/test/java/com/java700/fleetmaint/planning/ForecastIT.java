package com.java700.fleetmaint.planning;

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

/** Meter/calendar scheduling engine: due-soon forecasting, overdue marking, idempotency. */
@SpringBootTest(classes = com.java700.fleetmaint.FleetMaintenanceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ForecastIT {

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

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "fleet", Roles.FLEET_MANAGER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "driver", Roles.DRIVER, null);
        fleet = TestFixtures.token(mvc, mapper, "fleet");
        driver = TestFixtures.token(mvc, mapper, "driver");
    }

    private String registerTruck(String plate, int odometer, int anchorOdometer,
                                 int lastServiceDaysAgo) throws Exception {
        String body = String.format(
                "{\"vin\":\"VIN-%s\",\"plate\":\"%s\",\"make\":\"Volvo\",\"model\":\"FH16\","
                        + "\"modelYear\":2022,\"category\":\"TRUCK\",\"initialOdometer\":%d,"
                        + "\"serviceAnchorOdometer\":%d,\"lastServiceDate\":\"%s\","
                        + "\"department\":\"LOGISTICS\",\"driverName\":\"Ana Dias\"}",
                plate, plate, odometer, anchorOdometer,
                LocalDate.now(clock).minusDays(lastServiceDaysAgo));
        String response = mvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + fleet)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("id").asText();
    }

    private JsonNode runForecast() throws Exception {
        String body = mvc.perform(post("/api/v1/scheduling/forecast/run")
                        .header("Authorization", "Bearer " + fleet))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }

    @Test
    void forecastCreatesDueAndOverdueTasksWithCorrectPriorities() throws Exception {
        // Oil due at 100,000 (anchor 90,000 + 10,000): current 98,500 -> remaining 1,500 -> DUE.
        // DOT annual: last service 370 days ago -> next due 5 days ago -> OVERDUE, COMPLIANCE.
        String vehicleId = registerTruck("PLT-100", 98500, 90000, 370);

        JsonNode result = runForecast();
        assertThat(result.get("created").asInt()).isGreaterThanOrEqualTo(2);

        String tasksBody = mvc.perform(get("/api/v1/scheduling/tasks")
                        .header("Authorization", "Bearer " + fleet))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode tasks = mapper.readTree(tasksBody);
        assertThat(tasks).isNotEmpty();

        JsonNode dotTask = null;
        JsonNode oilTask = null;
        for (JsonNode task : tasks) {
            if (vehicleId.equals(task.get("vehicleId").asText())
                    && "DOT-ANNUAL".equals(task.get("planCode").asText())) {
                dotTask = task;
            }
            if (vehicleId.equals(task.get("vehicleId").asText())
                    && "OIL-CHANGE".equals(task.get("planCode").asText())) {
                oilTask = task;
            }
        }
        assertThat(dotTask).isNotNull();
        assertThat(dotTask.get("status").asText()).isEqualTo("OVERDUE");
        assertThat(dotTask.get("priority").asText()).isEqualTo("COMPLIANCE");
        assertThat(oilTask).isNotNull();
        assertThat(oilTask.get("status").asText()).isEqualTo("DUE");
        assertThat(oilTask.get("dueOdometer").asInt()).isEqualTo(100000);
    }

    @Test
    void forecastIsIdempotentPerVehicleAndPlan() throws Exception {
        registerTruck("PLT-101", 98500, 90000, 50);
        JsonNode first = runForecast();
        JsonNode second = runForecast();
        assertThat(second.get("created").asInt()).isZero();
        assertThat(first.get("created").asInt()).isGreaterThan(0);
    }

    @Test
    void odometerPastDuePointFlipsTaskToOverdue() throws Exception {
        registerTruck("PLT-102", 99500, 90000, 50);
        JsonNode first = runForecast();
        assertThat(first.get("created").asInt()).isGreaterThanOrEqualTo(1);

        String vehiclesBody = mvc.perform(get("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + fleet))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String vehicleId = mapper.readTree(vehiclesBody).get(0).get("id").asText();
        mvc.perform(post("/api/v1/vehicles/" + vehicleId + "/odometer")
                        .header("Authorization", "Bearer " + driver)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reading\":100600,\"source\":\"DRIVER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(true));

        JsonNode second = runForecast();
        assertThat(second.get("overdue").asInt()).isGreaterThanOrEqualTo(1);
        String tasksBody = mvc.perform(get("/api/v1/scheduling/tasks")
                        .header("Authorization", "Bearer " + fleet))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode oil = null;
        for (JsonNode task : mapper.readTree(tasksBody)) {
            if ("OIL-CHANGE".equals(task.get("planCode").asText())) {
                oil = task;
            }
        }
        assertThat(oil).isNotNull();
        assertThat(oil.get("status").asText()).isEqualTo("OVERDUE");
    }

    @Test
    void driverCannotRunForecast() throws Exception {
        mvc.perform(post("/api/v1/scheduling/forecast/run")
                        .header("Authorization", "Bearer " + driver))
                .andExpect(status().isForbidden());
    }
}
