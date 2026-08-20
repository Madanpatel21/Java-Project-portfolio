package com.java700.p2p.flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.p2p.common.TestDb;
import com.java700.p2p.common.TestFixtures;
import com.java700.p2p.domain.InvoiceRepository;
import com.java700.p2p.security.LocalUserService;
import com.java700.p2p.security.Roles;
import java.time.Clock;
import java.time.Instant;
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

/** End-to-end P2P: PO → GR → invoice → match → exception → waiver → posting. */
@SpringBootTest(classes = com.java700.p2p.ProcureToPayApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class P2PFlowIT {

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
    InvoiceRepository invoices;

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

    private String poWithReceipt(String poNumber, String itemCode, String qty, String price,
                                 String receivedQty) throws Exception {
        String poBody = mvc.perform(post("/api/v1/po")
                        .header("Authorization", "Bearer " + procurement)
                        .header("Idempotency-Key", "po-" + poNumber)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "poNumber", poNumber, "supplierId", "SUP-1",
                                "supplierName", "Acme Supplies Ltd", "currency", "USD",
                                "lines", List.of(Map.of(
                                        "itemCode", itemCode, "description", "Widget",
                                        "quantity", qty, "unitPrice", price))))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String poId = poBody;
        // find the PO line id via DB
        String poLineId = jdbc.queryForObject(
                "select id from purchase_order_lines where po_id = ?", String.class, poId);
        mvc.perform(post("/api/v1/gr")
                        .header("Authorization", "Bearer " + procurement)
                        .header("Idempotency-Key", "gr-" + poNumber)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "grNumber", "GR-" + poNumber, "poId", poId,
                                "lines", List.of(Map.of(
                                        "poLineId", poLineId, "quantityReceived", receivedQty))))))
                .andExpect(status().isOk());
        return poId;
    }

    private String ingest(String invoiceNumber, String itemCode, String qty, String price)
            throws Exception {
        String body = mvc.perform(post("/api/v1/invoices")
                        .header("Authorization", "Bearer " + clerk)
                        .header("Idempotency-Key", "inv-" + invoiceNumber)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "invoiceNumber", invoiceNumber, "supplierId", "SUP-1",
                                "supplierName", "Acme Supplies Ltd", "currency", "USD",
                                "totalAmount", new java.math.BigDecimal(qty).multiply(new java.math.BigDecimal(price)),
                                "invoiceDate", "2026-08-05",
                                "lines", List.of(Map.of(
                                        "itemCode", itemCode, "quantity", qty,
                                        "unitPrice", price,
                                        "lineTotal", new java.math.BigDecimal(qty).multiply(new java.math.BigDecimal(price))))))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("id").asText();
    }

    @Test
    void poViewExposesLinesWithReceivedQuantities() throws Exception {
        String poId = poWithReceipt("PO-VIEW", "WIDGET-2000", "100", "10.0000", "100");
        mvc.perform(get("/api/v1/po/" + poId).header("Authorization", "Bearer " + clerk))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.poNumber").value("PO-VIEW"))
                .andExpect(jsonPath("$.lines[0].receivedQty").value(100));
    }

    @Test
    void cleanThreeWayMatchFlowsToMatched() throws Exception {
        poWithReceipt("PO-A", "WIDGET-2000", "100", "10.0000", "100");
        String invId = ingest("INV-A", "WIDGET-2000", "100", "10.0000");
        mvc.perform(get("/api/v1/invoices").header("Authorization", "Bearer " + clerk))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].status").value("MATCHED"));
    }

    @Test
    void invoiceIngestIsIdempotent() throws Exception {
        poWithReceipt("PO-B", "WIDGET-2000", "100", "10.0000", "100");
        String first = mvc.perform(post("/api/v1/invoices")
                        .header("Authorization", "Bearer " + clerk)
                        .header("Idempotency-Key", "inv-idem-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "invoiceNumber", "INV-IDEM", "supplierId", "SUP-1",
                                "supplierName", "Acme Supplies Ltd", "currency", "USD",
                                "totalAmount", 1000.00, "invoiceDate", "2026-08-05",
                                "lines", List.of(Map.of(
                                        "itemCode", "WIDGET-2000", "quantity", "100",
                                        "unitPrice", "10.0000", "lineTotal", 1000.00))))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String replay = mvc.perform(post("/api/v1/invoices")
                        .header("Authorization", "Bearer " + clerk)
                        .header("Idempotency-Key", "inv-idem-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "invoiceNumber", "INV-IDEM", "supplierId", "SUP-1",
                                "supplierName", "Acme Supplies Ltd", "currency", "USD",
                                "totalAmount", 1000.00, "invoiceDate", "2026-08-05",
                                "lines", List.of(Map.of(
                                        "itemCode", "WIDGET-2000", "quantity", "100",
                                        "unitPrice", "10.0000", "lineTotal", 1000.00))))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(mapper.readTree(replay).get("id").asText())
                .isEqualTo(mapper.readTree(first).get("id").asText());
        assertThat(invoices.count()).isEqualTo(1);
    }

    @Test
    void duplicateInvoiceNumberIsRejected() throws Exception {
        poWithReceipt("PO-C", "WIDGET-2000", "100", "10.0000", "100");
        ingest("INV-DUP", "WIDGET-2000", "100", "10.0000");
        mvc.perform(post("/api/v1/invoices")
                        .header("Authorization", "Bearer " + clerk)
                        .header("Idempotency-Key", "inv-dup-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "invoiceNumber", "INV-DUP", "supplierId", "SUP-1",
                                "supplierName", "Acme Supplies Ltd", "currency", "USD",
                                "totalAmount", 1000.00, "invoiceDate", "2026-08-05",
                                "lines", List.of(Map.of(
                                        "itemCode", "WIDGET-2000", "quantity", "100",
                                        "unitPrice", "10.0000", "lineTotal", 1000.00))))))
                .andExpect(status().isConflict());
    }

    @Test
    void priceVarianceExceptionWaivedByManagerThenPosted() throws Exception {
        poWithReceipt("PO-D", "WIDGET-2000", "100", "10.0000", "100");
        String invId = ingest("INV-D", "WIDGET-2000", "100", "10.9000"); // 9% over PO price
        mvc.perform(get("/api/v1/invoices").header("Authorization", "Bearer " + clerk))
                .andExpect(jsonPath("$.items[0].status").value("EXCEPTION"));

        String open = mvc.perform(get("/api/v1/exceptions").header("Authorization", "Bearer " + clerk))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode exceptions = mapper.readTree(open);
        assertThat(exceptions.get("items")).isNotEmpty();
        String exId = exceptions.get("items").get(0).get("id").asText();

        // clerk cannot waive (four-eyes)
        mvc.perform(post("/api/v1/exceptions/" + exId + "/waive")
                        .header("Authorization", "Bearer " + clerk)
                        .header("Idempotency-Key", "waive-clerk-1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());

        // manager waives → invoice APPROVED
        mvc.perform(post("/api/v1/exceptions/" + exId + "/waive")
                        .header("Authorization", "Bearer " + manager)
                        .header("Idempotency-Key", "waive-mgr-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"Supplier contract price update approved\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAIVED"));

        mvc.perform(get("/api/v1/invoices").header("Authorization", "Bearer " + clerk))
                .andExpect(jsonPath("$.items[0].status").value("APPROVED"));

        // posting batch
        mvc.perform(post("/api/v1/batch").header("Authorization", "Bearer " + manager))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/invoices").header("Authorization", "Bearer " + clerk))
                .andExpect(jsonPath("$.items[0].status").value("POSTED"));
        Integer postings = jdbc.queryForObject("select count(*) from gl_postings", Integer.class);
        assertThat(postings).isEqualTo(2); // debit GRNI + credit AP
    }

    @Test
    void auditorIsReadOnly() throws Exception {
        mvc.perform(get("/api/v1/invoices").header("Authorization", "Bearer " + auditor))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/batch").header("Authorization", "Bearer " + auditor))
                .andExpect(status().isForbidden());
    }
}
