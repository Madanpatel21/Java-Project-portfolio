package com.java700.roster.security;

import com.java700.roster.common.audit.AuditLogService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dev/test token endpoint for the built-in local identity provider. */
@RestController
@RequestMapping("/api/v1/auth")
@Profile({"dev", "test"})
public class DevTokenController {

    private final LocalUserService userService;
    private final JwtEncoder encoder;
    private final AuditLogService audit;
    private final Clock clock;
    private final String issuer;
    private final long ttlMinutes;

    public DevTokenController(LocalUserService userService, AuditLogService audit, Clock clock,
                              @Value("${app.security.jwt.secret}") String secret,
                              @Value("${app.security.jwt.issuer:http://localhost:8080}") String issuer,
                              @Value("${app.security.jwt.ttl-minutes:30}") long ttlMinutes) {
        this.userService = userService;
        this.audit = audit;
        this.clock = clock;
        this.issuer = issuer;
        this.ttlMinutes = ttlMinutes;
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @PostMapping("/token")
    ResponseEntity<TokenResponse> token(@Valid @RequestBody TokenRequest request) {
        var user = userService.authenticate(request.username(), request.password());
        Instant now = Instant.now(clock);
        List<String> roles = userService.rolesOf(user.getId());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(ttlMinutes, ChronoUnit.MINUTES))
                .subject(user.getId())
                .id(UUID.randomUUID().toString())
                .claim("preferred_username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("office_id", user.getOfficeId() == null ? "" : user.getOfficeId())
                .claim("roles", roles)
                .build();
        String token = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        audit.record("TOKEN_ISSUED", "LOCAL_USER", user.getId(), "Bearer token issued (ttl " + ttlMinutes + "m)");
        return ResponseEntity.ok(new TokenResponse(token, "Bearer", ttlMinutes * 60,
                user.getUsername(), roles));
    }

    public record TokenRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds,
                                String username, List<String> roles) {

        public TokenResponse {
            roles = List.copyOf(roles);
        }
    }
}
