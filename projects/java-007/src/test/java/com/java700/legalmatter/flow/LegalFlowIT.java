package com.java700.legalmatter.flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.legalmatter.common.TestDb;
import com.java700.legalmatter.common.TestFixtures;
import com.java700.legalmatter.domain.MatterDeadlineRepository;
import com.java700.legalmatter.security.LocalUserService;
import com.java700.legalmatter.security.Roles;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

/** End-to-end: parties/matter graph, conflict screening, court deadlines, ethical walls. */
@SpringBootTest(classes = com.java700.legalmatter.LegalMatterApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LegalFlowIT {

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
    @Autowired
    MatterDeadlineRepository deadlines;

    private String attorney;
    private String paralegal;
    private String analyst;
    private String litteam;
    private String auditor;
    private String admin;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "attorney", Roles.ATTORNEY, null);
        TestFixtures.createUser(localUsers, encoder, clock, "paralegal", Roles.PARALEGAL, null);
        TestFixtures.createUser(localUsers, encoder, clock, "analyst", Roles.CONFLICT_ANALYST, null);
        TestFixtures.createUser(localUsers, encoder, clock, "litteam", Roles.LITIGATION_TEAM, null);
        TestFixtures.createUser(localUsers, encoder, clock, "auditor", Roles.AUDITOR, null);
        TestFixtures.createUser(localUsers, encoder, clock, "admin", Roles.ADMIN, null);
        attorney = TestFixtures.token(mvc, mapper, "attorney");
        paralegal = TestFixtures.token(mvc, mapper, "paralegal");
        analyst = TestFixtures.token(mvc, mapper, "analyst");
        litteam = TestFixtures.token(mvc, mapper, "litteam");
        auditor = TestFixtures.token(mvc, mapper, "auditor");
        admin = TestFixtures.token(mvc, mapper, "admin");
    }

    private String party(String name, String type, String idem) throws Exception {
        String body = mvc.perform(post("/api/v1/parties")
                        .header("Authorization", "Bearer " + attorney)
                        .header("Idempotency-Key", idem)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("name", name, "type", type))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return body;
    }

    private String matter(String no, String name, String clientId) throws Exception {
        return mvc.perform(post("/api/v1/matters")
                        .header("Authorization", "Bearer " + attorney)
                        .header("Idempotency-Key", "m-" + no)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "matterNo", no, "name", name, "clientPartyId", clientId,
                                "practiceArea", "LITIGATION"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void conflictDetectionAndDeadlinesAndWalls() throws Exception {
        String acme = party("Acme Corporation", "CLIENT", "p1");
        String beta = party("Beta Industries", "OPPONENT", "p2");
        String matterId = matter("M-2026-001", "Acme v Beta", acme);
        mvc.perform(post("/api/v1/matters/" + matterId + "/parties")
                        .header("Authorization", "Bearer " + attorney)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("partyId", beta, "role", "OPPOSING"))))
                .andExpect(status().isOk());

        // prospective client Delta vs Beta → CONFLICT (Beta adverse to existing client Acme)
        mvc.perform(post("/api/v1/conflicts/screen")
                        .header("Authorization", "Bearer " + analyst)
                        .header("Idempotency-Key", "scr-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "subjectName", "Delta Corp",
                                "adverseNames", List.of("Beta Industries")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("CONFLICT"));

        // clean screen
        mvc.perform(post("/api/v1/conflicts/screen")
                        .header("Authorization", "Bearer " + analyst)
                        .header("Idempotency-Key", "scr-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "subjectName", "Epsilon LLC",
                                "adverseNames", List.of("Zeta GmbH")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("CLEAR"));

        // deadline rules for jurisdiction DEFAULT are seeded by V3 migration
        mvc.perform(post("/api/v1/matters/" + matterId + "/deadlines")
                        .header("Authorization", "Bearer " + paralegal)
                        .header("Idempotency-Key", "dl-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "jurisdiction", "DEFAULT",
                                "triggerDate", LocalDate.of(2026, 8, 20).toString()))))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/matters/" + matterId + "/deadlines")
                        .header("Authorization", "Bearer " + paralegal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].dueAt").value("2026-08-13")); // APPEAL -7d sorts first

        // ethical wall blocks LITIGATION_TEAM from the matter
        mvc.perform(post("/api/v1/matters/" + matterId + "/walls")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"LITIGATION_TEAM\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/matters/" + matterId).header("Authorization", "Bearer " + litteam))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/v1/matters/" + matterId).header("Authorization", "Bearer " + attorney))
                .andExpect(status().isOk());
    }

    @Test
    void missedDeadlineDetection() throws Exception {
        String acme = party("Acme Corp", "CLIENT", "p3");
        String matterId = matter("M-2026-002", "Acme v Zeta", acme);
        mvc.perform(post("/api/v1/matters/" + matterId + "/deadlines")
                        .header("Authorization", "Bearer " + paralegal)
                        .header("Idempotency-Key", "dl-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "jurisdiction", "DEFAULT",
                                "triggerDate", LocalDate.of(2026, 1, 1).toString()))))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/deadlines/mark-missed").header("Authorization", "Bearer " + paralegal))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/matters/" + matterId + "/deadlines")
                        .header("Authorization", "Bearer " + paralegal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("MISSED"));
    }
}
