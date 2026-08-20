package com.java700.fleetmaint.workflow;

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

/** Work order lifecycle: parts kitting, reservations, holds, completion and rejection. */
@SpringBootTest(classes = com.java700.fleetmaint.FleetMaintenanceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkOrderIT {

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
    private String mechanic;
    private String clerk;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        // Re-seed parts inventory (cleared by TestDb; V3 seeds only plans).
        jdbc.update("insert into parts (id, part_code, name, quantity_on_hand, reserved_qty,"
                + " reorder_point, unit_cost) values"
                + " ('pt-001','OIL-5W30','Engine Oil 5W-30 (5L)',40,0,10,35.00),"
                + " ('pt-002','FILT-OIL','Oil Filter',60,0,15,9.50),"
                + " ('pt-003','PAD-FRONT','Front Brake Pad Set',6,0,4,80.00),"
                + " ('pt-004','SENS-ABS','ABS Sensor',8,0,4,30.00),"
                + " ('pt-005','WHEEL-WGT','Wheel Balance Weights',50,0,20,4.75)");
        TestFixtures.createUser(localUsers, encoder, clock, "fleet", Roles.FLEET_MANAGER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "mechanic", Roles.MECHANIC, null);
        TestFixtures.createUser(localUsers, encoder, clock, "clerk", Roles.PARTS_CLERK, null);
        fleet = TestFixtures.token(mvc, mapper, "fleet");
        mechanic = TestFixtures.token(mvc, mapper, "mechanic");
        clerk = TestFixtures.token(mvc, mapper, "clerk");
    }

    private JsonNode dueOilTask() throws Exception {
        String vehicleBody = String.format(
                "{\"vin\":\"VIN-WO\",\"plate\":\"WO-001\",\"make\":\"Scania\",\"model\":\"R450\","
                        + "\"modelYear\":2021,\"category\":\"TRUCK\",\"initialOdometer\":99000,"
                        + "\"serviceAnchorOdometer\":90000,\"lastServiceDate\":\"%s\"}",
                java.time.LocalDate.now(clock).minusDays(20));
        mvc.perform(post("/api/v1/vehicles").header("Authorization", "Bearer " + fleet)
                        .contentType(MediaType.APPLICATION_JSON).content(vehicleBody))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/scheduling/forecast/run")
                .header("Authorization", "Bearer " + fleet)).andExpect(status().isOk());
        String tasks = mvc.perform(get("/api/v1/scheduling/tasks")
                        .header("Authorization", "Bearer " + fleet))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (JsonNode task : mapper.readTree(tasks)) {
            if ("OIL-CHANGE".equals(task.get("planCode").asText())) {
                return task;
            }
        }
        throw new AssertionError("no oil task forecasted");
    }

    private JsonNode openWorkOrder(String taskId) throws Exception {
        String body = mvc.perform(post("/api/v1/work-orders")
                        .header("Authorization", "Bearer " + fleet)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taskId\":\"" + taskId + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }

    @Test
    void workOrderLifecycleReservesAndIssuesParts() throws Exception {
        JsonNode task = dueOilTask();
        assertThat(task.get("status").asText()).isEqualTo("DUE");

        JsonNode workOrder = openWorkOrder(task.get("id").asText());
        assertThat(workOrder.get("status").asText()).isEqualTo("OPEN");
        assertThat(workOrder.get("reservations")).hasSize(2);
        assertThat(workOrder.get("reservations").get(0).get("status").asText())
                .isEqualTo("RESERVED");

        // Parts were reserved: 2 of 40 oil units, 1 of 60 filters.
        String parts = mvc.perform(get("/api/v1/inventory/parts")
                        .header("Authorization", "Bearer " + clerk))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (JsonNode part : mapper.readTree(parts)) {
            if ("OIL-5W30".equals(part.get("partCode").asText())) {
                assertThat(part.get("reservedQty").asInt()).isEqualTo(1);
            }
        }

        mvc.perform(post("/api/v1/work-orders/" + workOrder.get("id").asText() + "/start")
                        .header("Authorization", "Bearer " + mechanic)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mechanic\":\"Bob\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mvc.perform(post("/api/v1/work-orders/" + workOrder.get("id").asText() + "/complete")
                        .header("Authorization", "Bearer " + mechanic)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mechanic\":\"Bob\",\"laborHours\":1.5,\"laborCost\":90.00,"
                                + "\"odometerAtService\":100150,\"note\":\"done\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.partsCost").value(44.50))
                .andExpect(jsonPath("$.totalCost").value(134.50));

        // Parts were issued: on-hand dropped from 40 to 39.
        String after = mvc.perform(get("/api/v1/inventory/parts")
                        .header("Authorization", "Bearer " + clerk))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (JsonNode part : mapper.readTree(after)) {
            if ("OIL-5W30".equals(part.get("partCode").asText())) {
                assertThat(part.get("quantityOnHand").asInt()).isEqualTo(39);
                assertThat(part.get("reservedQty").asInt()).isZero();
            }
        }

        // Task completed and vehicle odometer advanced (no open OIL task remains).
        String tasks = mvc.perform(get("/api/v1/scheduling/tasks")
                        .header("Authorization", "Bearer " + fleet))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (JsonNode open : mapper.readTree(tasks)) {
            assertThat(open.get("planCode").asText()).isNotEqualTo("OIL-CHANGE");
        }
        String vehicles = mvc.perform(get("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + fleet))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(mapper.readTree(vehicles).get(0).get("currentOdometer").asInt())
                .isEqualTo(100150);
    }

    @Test
    void partsShortfallHoldsWorkOrderUntilRestock() throws Exception {
        // Custom plan whose kit needs 10 brake pad sets while only 6 exist.
        String planBody = "{\"code\":\"BRAKE-OVERHAUL\",\"name\":\"Brake Overhaul\","
                + "\"appliesToCategory\":\"ANY\",\"intervalType\":\"ODOMETER\","
                + "\"intervalValue\":50000,\"complianceRequired\":false,"
                + "\"items\":[{\"partCode\":\"PAD-FRONT\",\"partName\":\"Front Brake Pad Set\","
                + "\"quantity\":10,\"estimatedCost\":80.00}]}";
        mvc.perform(post("/api/v1/plans").header("Authorization", "Bearer " + fleet)
                        .contentType(MediaType.APPLICATION_JSON).content(planBody))
                .andExpect(status().isCreated());

        String vehicleBody = "{\"vin\":\"VIN-HOLD\",\"plate\":\"HOLD-01\",\"make\":\"Volvo\","
                + "\"model\":\"FM\",\"modelYear\":2020,\"category\":\"TRUCK\","
                + "\"initialOdometer\":140000,\"serviceAnchorOdometer\":90000,"
                + "\"lastServiceDate\":\"2025-01-10\"}";
        mvc.perform(post("/api/v1/vehicles").header("Authorization", "Bearer " + fleet)
                        .contentType(MediaType.APPLICATION_JSON).content(vehicleBody))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/scheduling/forecast/run")
                .header("Authorization", "Bearer " + fleet)).andExpect(status().isOk());
        String tasks = mvc.perform(get("/api/v1/scheduling/tasks")
                        .header("Authorization", "Bearer " + fleet))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode task = null;
        for (JsonNode candidate : mapper.readTree(tasks)) {
            if ("BRAKE-OVERHAUL".equals(candidate.get("planCode").asText())) {
                task = candidate;
            }
        }
        assertThat(task).isNotNull();

        JsonNode workOrder = openWorkOrder(task.get("id").asText());
        assertThat(workOrder.get("status").asText()).isEqualTo("PARTS_HOLD");
        assertThat(workOrder.get("shortfallReason").asText()).contains("Front Brake Pad Set");

        // Starting while on hold is refused.
        mvc.perform(post("/api/v1/work-orders/" + workOrder.get("id").asText() + "/start")
                        .header("Authorization", "Bearer " + mechanic)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mechanic\":\"Bob\"}"))
                .andExpect(status().isConflict());

        // Clerk restocks; retry resolves the hold.
        mvc.perform(post("/api/v1/inventory/parts/PAD-FRONT/restock")
                        .header("Authorization", "Bearer " + clerk)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":10}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/work-orders/" + workOrder.get("id").asText() + "/retry-parts")
                        .header("Authorization", "Bearer " + clerk))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void rejectionReleasesReservationsAndReopensTask() throws Exception {
        JsonNode task = dueOilTask();
        JsonNode workOrder = openWorkOrder(task.get("id").asText());
        mvc.perform(post("/api/v1/work-orders/" + workOrder.get("id").asText() + "/reject")
                        .header("Authorization", "Bearer " + fleet)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"vehicle reassigned\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        String parts = mvc.perform(get("/api/v1/inventory/parts")
                        .header("Authorization", "Bearer " + clerk))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (JsonNode part : mapper.readTree(parts)) {
            if ("OIL-5W30".equals(part.get("partCode").asText())) {
                assertThat(part.get("reservedQty").asInt()).isZero();
            }
        }
        String tasks = mvc.perform(get("/api/v1/scheduling/tasks")
                        .header("Authorization", "Bearer " + fleet))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode oil = null;
        for (JsonNode open : mapper.readTree(tasks)) {
            if ("OIL-CHANGE".equals(open.get("planCode").asText())) {
                oil = open;
            }
        }
        assertThat(oil).isNotNull();
        assertThat(oil.get("status").asText()).isEqualTo("DUE");
    }

    @Test
    void clerkCannotCompleteWorkOrders() throws Exception {
        JsonNode task = dueOilTask();
        JsonNode workOrder = openWorkOrder(task.get("id").asText());
        mvc.perform(post("/api/v1/work-orders/" + workOrder.get("id").asText() + "/start")
                        .header("Authorization", "Bearer " + mechanic)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mechanic\":\"Bob\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/work-orders/" + workOrder.get("id").asText() + "/complete")
                        .header("Authorization", "Bearer " + clerk)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mechanic\":\"x\",\"laborHours\":1,\"laborCost\":10}"))
                .andExpect(status().isForbidden());
    }
}
