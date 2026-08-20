package com.java700.govault.flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.govault.common.TestDb;
import com.java700.govault.common.TestFixtures;
import com.java700.govault.domain.DocumentRepository;
import com.java700.govault.security.LocalUserService;
import com.java700.govault.security.Roles;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** End-to-end: upload → quarantine → classify → hold → retention scan → disposition proof. */
@SpringBootTest(classes = com.java700.govault.DocumentGovernanceApplication.class,
        properties = "app.govault.content-dir=target/test-content")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GovernanceFlowIT {

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
    DocumentRepository documents;

    private String records;
    private String legal;
    private String auditor;
    private String admin;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "records", Roles.RECORDS_MANAGER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "legal", Roles.LEGAL_COUNSEL, null);
        TestFixtures.createUser(localUsers, encoder, clock, "auditor", Roles.AUDITOR, null);
        TestFixtures.createUser(localUsers, encoder, clock, "admin", Roles.ADMIN, null);
        records = TestFixtures.token(mvc, mapper, "records");
        legal = TestFixtures.token(mvc, mapper, "legal");
        auditor = TestFixtures.token(mvc, mapper, "auditor");
        admin = TestFixtures.token(mvc, mapper, "admin");
    }

    private String upload(String fileName, String text, String idem) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", fileName, "text/plain",
                text.getBytes());
        String body = mvc.perform(multipart("/api/v1/documents")
                        .file(file)
                        .header("Authorization", "Bearer " + records)
                        .header("Idempotency-Key", idem))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("QUARANTINED"))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("id").asText();
    }

    @Test
    void fullGovernanceLoopWithHoldAndDispositionProof() throws Exception {
        String docId = upload("contract.txt", "Confidential pricing terms for Acme deal", "up-1");
        // classify
        mvc.perform(post("/api/v1/documents/" + docId + "/classify")
                        .header("Authorization", "Bearer " + records)
                        .header("Idempotency-Key", "cl-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"classification\":\"CONFIDENTIAL\",\"retentionClass\":\"R0\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.classification").value("CONFIDENTIAL"));

        // legal hold blocks disposition
        String hold = mvc.perform(post("/api/v1/holds")
                        .header("Authorization", "Bearer " + legal)
                        .header("Idempotency-Key", "h-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Litigation 2026-Q3\",\"reason\":\"Pending discovery\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String holdId = mapper.readTree(hold).get("id").asText();
        mvc.perform(post("/api/v1/holds/" + holdId + "/apply/" + docId)
                        .header("Authorization", "Bearer " + legal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // backdate the upload to trip retention, then scan → protected
        jdbc.update("update documents set uploaded_at = ? where id = ?",
                java.sql.Timestamp.from(Instant.now(clock).minusSeconds(31L * 86400)), docId);
        mvc.perform(post("/api/v1/retention/scan").header("Authorization", "Bearer " + records))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/documents").header("Authorization", "Bearer " + records))
                .andExpect(jsonPath("$.items[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.items[0].legalHold").value(true));

        // release hold → scan disposes with proof
        mvc.perform(post("/api/v1/holds/" + holdId + "/release")
                        .header("Authorization", "Bearer " + legal))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/retention/scan").header("Authorization", "Bearer " + records))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/documents").header("Authorization", "Bearer " + records))
                .andExpect(jsonPath("$.items[0].status").value("DISPOSED"));

        // proof exists with the content hash
        mvc.perform(get("/api/v1/documents/" + docId + "/disposition-proofs")
                        .header("Authorization", "Bearer " + auditor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].documentId").value(docId))
                .andExpect(jsonPath("$[0].disposition").value("DISPOSED"));

        // disposed content cannot be downloaded
        mvc.perform(get("/api/v1/documents/" + docId + "/download")
                        .header("Authorization", "Bearer " + records))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadEnforcesClassificationClearance() throws Exception {
        String docId = upload("pricing.txt", "RESTRICTED pricing data", "up-2");
        mvc.perform(post("/api/v1/documents/" + docId + "/classify")
                        .header("Authorization", "Bearer " + records)
                        .header("Idempotency-Key", "cl-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"classification\":\"RESTRICTED\",\"retentionClass\":\"R1\"}"))
                .andExpect(status().isOk());
        // records manager (clearance 4) can download
        mvc.perform(get("/api/v1/documents/" + docId + "/download")
                        .header("Authorization", "Bearer " + records))
                .andExpect(status().isOk());
        // auditor (clearance 2) cannot
        mvc.perform(get("/api/v1/documents/" + docId + "/download")
                        .header("Authorization", "Bearer " + auditor))
                .andExpect(status().isConflict());
    }

    @Test
    void fullTextSearchFindsExtractedContent() throws Exception {
        String docId = upload("notes.md", "Quarterly board minutes mention Project Falcon", "up-3");
        mvc.perform(get("/api/v1/documents/search").header("Authorization", "Bearer " + records)
                        .param("q", "Falcon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(docId));
    }

    @Test
    void duplicateHoldApplicationIsRejected() throws Exception {
        String docId = upload("a.txt", "content", "up-4");
        mvc.perform(post("/api/v1/documents/" + docId + "/classify")
                        .header("Authorization", "Bearer " + records)
                        .header("Idempotency-Key", "cl-4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"classification\":\"INTERNAL\",\"retentionClass\":\"R1\"}"))
                .andExpect(status().isOk());
        String hold = mvc.perform(post("/api/v1/holds")
                        .header("Authorization", "Bearer " + legal)
                        .header("Idempotency-Key", "h-4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hold A\",\"reason\":\"R\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String holdId = mapper.readTree(hold).get("id").asText();
        mvc.perform(post("/api/v1/holds/" + holdId + "/apply/" + docId)
                        .header("Authorization", "Bearer " + legal))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/holds/" + holdId + "/apply/" + docId)
                        .header("Authorization", "Bearer " + legal))
                .andExpect(status().isConflict());
    }

    @Test
    void uploadIsIdempotent() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "dup.txt", "text/plain",
                "same content".getBytes());
        String first = mvc.perform(multipart("/api/v1/documents").file(file)
                        .header("Authorization", "Bearer " + records)
                        .header("Idempotency-Key", "dup-1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        MockMultipartFile file2 = new MockMultipartFile("file", "dup.txt", "text/plain",
                "same content".getBytes());
        String replay = mvc.perform(multipart("/api/v1/documents").file(file2)
                        .header("Authorization", "Bearer " + records)
                        .header("Idempotency-Key", "dup-1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(mapper.readTree(replay).get("id").asText())
                .isEqualTo(mapper.readTree(first).get("id").asText());
        assertThat(documents.count()).isEqualTo(1);
    }
}
