package com.java700.roster.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.roster.security.LocalUser;
import com.java700.roster.security.LocalUserService;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Shared test helpers: user creation and token issuance. */
public final class TestFixtures {

    public static final String PASSWORD = "Password123!";

    private TestFixtures() {
    }

    public static String createUser(LocalUserService localUsers, PasswordEncoder encoder,
                                    Clock clock, String username, String role, String officeId) {
        String id = UUID.randomUUID().toString();
        localUsers.save(new LocalUser(id, username, encoder.encode(PASSWORD),
                username + "@registry.gov", officeId, Instant.now(clock)));
        localUsers.saveRole(id, role);
        return id;
    }

    public static String token(MockMvc mvc, ObjectMapper mapper, String username) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(body);
        return node.get("accessToken").asText();
    }
}
