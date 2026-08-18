package com.java700.crvs.registration;

import com.java700.crvs.registry.PersonRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.crvs.common.TestDb;
import com.java700.crvs.common.TestFixtures;
import com.java700.crvs.ledger.HashChain;
import com.java700.crvs.ledger.LifeEventRepository;
import com.java700.crvs.offices.OfficeRepository;
import com.java700.crvs.registry.Office;
import com.java700.crvs.registry.Person;
import com.java700.crvs.registry.PersonRepository;
import com.java700.crvs.security.LocalUserService;
import com.java700.crvs.security.Roles;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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

/**
 * End-to-end CRVS lifecycle: four-eyes birth registration, national-id assignment, ledger
 * chaining, deceased propagation, certificates and corrections.
 */
@SpringBootTest(classes = com.java700.crvs.CivilRegistryApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LifeEventFlowIT {

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
    OfficeRepository offices;
    @Autowired
    PersonRepository persons;
    @Autowired
    LifeEventRepository ledger;

    private String registrar;
    private String registrar2;
    private String supervisor;
    private String verifier;
    private String admin;
    private String officeId;
    private String office2Id;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        officeId = offices.save(new Office(UUID.randomUUID().toString(), "RO-N",
                "North Office", "NORTH")).getId();
        office2Id = offices.save(new Office(UUID.randomUUID().toString(), "RO-S",
                "South Office", "SOUTH")).getId();
        TestFixtures.createUser(localUsers, encoder, clock, "registrar", Roles.REGISTRAR, officeId);
        TestFixtures.createUser(localUsers, encoder, clock, "registrar2", Roles.REGISTRAR, office2Id);
        TestFixtures.createUser(localUsers, encoder, clock, "supervisor", Roles.SUPERVISOR, null);
        TestFixtures.createUser(localUsers, encoder, clock, "verifier", Roles.VERIFIER_CLIENT, null);
        TestFixtures.createUser(localUsers, encoder, clock, "admin", Roles.ADMIN, null);
        registrar = TestFixtures.token(mvc, mapper, "registrar");
        registrar2 = TestFixtures.token(mvc, mapper, "registrar2");
        supervisor = TestFixtures.token(mvc, mapper, "supervisor");
        verifier = TestFixtures.token(mvc, mapper, "verifier");
        admin = TestFixtures.token(mvc, mapper, "admin");
    }

    private String birthRegistration(String name, String idemKey) throws Exception {
        String body = mvc.perform(post("/api/v1/registrations/birth")
                        .header("Authorization", "Bearer " + registrar)
                        .header("Idempotency-Key", idemKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "fullName", name,
                                "dob", "1990-05-17",
                                "sex", "F",
                                "placeOfBirth", "London",
                                "parentNames", "Byron Lovelace, Anne Isabella"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("id").asText();
    }

    private String approve(String registrationId, String idemKey) throws Exception {
        String body = mvc.perform(post("/api/v1/registrations/" + registrationId + "/approve")
                        .header("Authorization", "Bearer " + supervisor)
                        .header("Idempotency-Key", idemKey)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("personId").asText();
    }

    @Test
    void birthFlowWithFourEyesCreatesPersonAndChainsLedger() throws Exception {
        String regId = birthRegistration("Ada Lovelace", "birth-ada-1");
        // A REGISTRAR may not approve at all — method security blocks first (403)
        mvc.perform(post("/api/v1/registrations/" + regId + "/approve")
                        .header("Authorization", "Bearer " + registrar)
                        .header("Idempotency-Key", "sod-block-1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());

        // idempotent replay of capture returns same registration
        String replay = mvc.perform(post("/api/v1/registrations/birth")
                        .header("Authorization", "Bearer " + registrar)
                        .header("Idempotency-Key", "birth-ada-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "fullName", "Ada Lovelace", "dob", "1990-05-17", "sex", "F",
                                "placeOfBirth", "London",
                                "parentNames", "Byron Lovelace, Anne Isabella"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(mapper.readTree(replay).get("id").asText()).isEqualTo(regId);

        // supervisor approves → person created with checksummed national id
        String personId = approve(regId, "approve-ada-1");
        Person person = persons.findById(personId).orElseThrow();
        assertThat(person.getNationalId()).hasSize(10);
        assertThat(com.java700.crvs.registry.NationalIdGenerator.isValid(person.getNationalId())).isTrue();
        assertThat(person.getStatus()).isEqualTo(Person.Status.ACTIVE);
        assertThat(person.getRegion()).isEqualTo("NORTH");

        // the registry-wide chain contains the BIRTH event and verifies cleanly
        var all = ledger.findAll();
        assertThat(all).hasSize(1);
        assertThat(HashChain.verify(all).valid()).isTrue();
    }

    @Test
    void dualRoleUserCannotApproveOwnCapture() throws Exception {
        // a user holding BOTH roles still cannot approve their own capture (domain SoD)
        String dualId = TestFixtures.createUser(localUsers, encoder, clock, "dualrole",
                Roles.REGISTRAR, officeId);
        localUsers.saveRole(dualId, Roles.SUPERVISOR);
        String dual = TestFixtures.token(mvc, mapper, "dualrole");
        String regId = mvc.perform(post("/api/v1/registrations/birth")
                        .header("Authorization", "Bearer " + dual)
                        .header("Idempotency-Key", "dual-birth-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "fullName", "Dual Role Person", "dob", "1990-01-01", "sex", "M",
                                "placeOfBirth", "Place", "parentNames", ""))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        regId = mapper.readTree(regId).get("id").asText();
        mvc.perform(post("/api/v1/registrations/" + regId + "/approve")
                        .header("Authorization", "Bearer " + dual)
                        .header("Idempotency-Key", "dual-approve-1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isConflict());
    }

    @Test
    void deathRegistrationPropagatesToVerification() throws Exception {
        String regId = birthRegistration("Alan Turing", "birth-alan-1");
        String personId = approve(regId, "approve-alan-1");
        Person person = persons.findById(personId).orElseThrow();
        String nid = person.getNationalId();

        // alive before death
        mvc.perform(get("/api/v1/verify/person/" + nid).header("Authorization", "Bearer " + verifier))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // registrar2 captures a death (different office is fine — cross-office registration)
        String deathId = mvc.perform(post("/api/v1/registrations/death")
                        .header("Authorization", "Bearer " + registrar2)
                        .header("Idempotency-Key", "death-alan-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "personId", personId, "cause", "Natural causes"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        deathId = mapper.readTree(deathId).get("id").asText();
        approve(deathId, "approve-death-alan-1");

        // verification now shows DECEASED
        mvc.perform(get("/api/v1/verify/person/" + nid).header("Authorization", "Bearer " + verifier))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DECEASED"))
                .andExpect(jsonPath("$.deceasedAt").exists());

        // duplicate death is rejected
        mvc.perform(post("/api/v1/registrations/death")
                        .header("Authorization", "Bearer " + registrar)
                        .header("Idempotency-Key", "death-alan-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "personId", personId, "cause", "Again"))))
                .andExpect(status().isConflict());

        // birth certificate for deceased is blocked
        mvc.perform(post("/api/v1/certificates")
                        .header("Authorization", "Bearer " + admin)
                        .header("Idempotency-Key", "cert-alan-birth-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("personId", personId, "type", "BIRTH"))))
                .andExpect(status().isConflict());

        // death certificate IS allowed
        mvc.perform(post("/api/v1/certificates")
                        .header("Authorization", "Bearer " + admin)
                        .header("Idempotency-Key", "cert-alan-death-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("personId", personId, "type", "DEATH"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VALID"));

        // per-person chain contains BIRTH + DEATH + CERTIFICATE_ISSUED and verifies
        var personChain = ledger.findByPersonIdOrderByChainSeqAsc(personId);
        assertThat(personChain).hasSize(3);
        assertThat(HashChain.verifyPerson(personChain).valid()).isTrue();
    }

    @Test
    void marriageLinksTwoActivePersonsAndBlocksDeceasedSpouse() throws Exception {
        String aId = approve(birthRegistration("Ada Lovelace", "m-birth-a"), "m-approve-a");
        String bId = approve(birthRegistration("Byron Lovelace", "m-birth-b"), "m-approve-b");
        // b is male
        Person b = persons.findById(bId).orElseThrow();
        b.applyCorrection(b.getFullName(), b.getDob(), b.getPlaceOfBirth(), b.getParentNames());
        persons.save(b);

        String marriageId = mvc.perform(post("/api/v1/registrations/marriage")
                        .header("Authorization", "Bearer " + registrar)
                        .header("Idempotency-Key", "marriage-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "personId", aId, "spousePersonId", bId))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        marriageId = mapper.readTree(marriageId).get("id").asText();
        approve(marriageId, "marriage-approve-1");

        // duplicate marriage blocked
        mvc.perform(post("/api/v1/registrations/marriage")
                        .header("Authorization", "Bearer " + registrar)
                        .header("Idempotency-Key", "marriage-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "personId", aId, "spousePersonId", bId))))
                .andExpect(status().isConflict());

        // kill b, then marriage to deceased is blocked
        String deathId = mvc.perform(post("/api/v1/registrations/death")
                        .header("Authorization", "Bearer " + registrar)
                        .header("Idempotency-Key", "death-b-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("personId", bId, "cause", "x"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        approve(mapper.readTree(deathId).get("id").asText(), "death-b-approve-1");

        String cId = approve(birthRegistration("Grace Hopper", "m-birth-c"), "m-approve-c");
        mvc.perform(post("/api/v1/registrations/marriage")
                        .header("Authorization", "Bearer " + registrar)
                        .header("Idempotency-Key", "marriage-3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "personId", cId, "spousePersonId", bId))))
                .andExpect(status().isConflict());
    }

    @Test
    void certificateVerificationAndRevocation() throws Exception {
        String personId = approve(birthRegistration("Cert Person", "cert-birth"), "cert-approve");
        String issued = mvc.perform(post("/api/v1/certificates")
                        .header("Authorization", "Bearer " + admin)
                        .header("Idempotency-Key", "cert-issue-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("personId", personId, "type", "BIRTH"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode cert = mapper.readTree(issued);
        String certId = cert.get("id").asText();
        String token = cert.get("token").asText();

        // valid verification reveals identity
        mvc.perform(get("/api/v1/certificates/verify/" + token)
                        .header("Authorization", "Bearer " + verifier))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.personName").value("Cert Person"));

        // revocation flips verification to invalid with masked identity
        mvc.perform(post("/api/v1/certificates/" + certId + "/revoke")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"issued in error\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/certificates/verify/" + token)
                        .header("Authorization", "Bearer " + verifier))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.personName").value("***"));

        // unknown token → 404
        mvc.perform(get("/api/v1/certificates/verify/does-not-exist")
                        .header("Authorization", "Bearer " + verifier))
                .andExpect(status().isNotFound());
    }

    @Test
    void correctionAmendsRecordPreservingOriginalInLedger() throws Exception {
        String personId = approve(birthRegistration("Ada Lovelace", "corr-birth"), "corr-approve");
        String corrId = mvc.perform(post("/api/v1/registrations/correction")
                        .header("Authorization", "Bearer " + registrar)
                        .header("Idempotency-Key", "corr-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "personId", personId, "field", "fullName",
                                "newValue", "Ada Augusta Lovelace", "reason", "Middle name omitted"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        approve(mapper.readTree(corrId).get("id").asText(), "corr-approve-1");

        Person person = persons.findById(personId).orElseThrow();
        assertThat(person.getFullName()).isEqualTo("Ada Augusta Lovelace");
        var chain = ledger.findByPersonIdOrderByChainSeqAsc(personId);
        assertThat(chain).hasSize(2);
        assertThat(chain.get(1).getEventType()).isEqualTo("AMENDMENT");
        assertThat(chain.get(1).getPayload()).contains("Ada Lovelace"); // original preserved
        assertThat(HashChain.verifyPerson(chain).valid()).isTrue();
    }

    @Test
    void dedupScanRaisesCandidateForNearDuplicateBirth() throws Exception {
        String first = approve(birthRegistration("Ada Lovelace", "dedup-birth-1"), "dedup-approve-1");
        // near-duplicate: same sex+dob+parents, one-letter name typo
        String second = mvc.perform(post("/api/v1/registrations/birth")
                        .header("Authorization", "Bearer " + registrar)
                        .header("Idempotency-Key", "dedup-birth-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "fullName", "Ada Lovelacee", "dob", "1990-05-17", "sex", "F",
                                "placeOfBirth", "London",
                                "parentNames", "Byron Lovelace, Anne Isabella"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        approve(mapper.readTree(second).get("id").asText(), "dedup-approve-2");

        // admin sees an OPEN candidate between the two persons
        String open = mvc.perform(get("/api/v1/dedup/open").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode candidates = mapper.readTree(open);
        assertThat(candidates).isNotEmpty();
        boolean links = false;
        for (JsonNode c : candidates) {
            String a = c.get("personAId").asText();
            String b = c.get("personBId").asText();
            if ((a.equals(first) && b.equals(personIdOf("Ada Lovelacee")))
                    || (b.equals(first) && a.equals(personIdOf("Ada Lovelacee")))) {
                links = true;
            }
        }
        assertThat(links).isTrue();
    }

    private String personIdOf(String name) {
        return persons.findAll().stream()
                .filter(p -> p.getFullName().equals(name))
                .findFirst().orElseThrow().getId();
    }
}
