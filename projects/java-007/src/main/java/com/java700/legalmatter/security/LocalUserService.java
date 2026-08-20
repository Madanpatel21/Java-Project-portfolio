package com.java700.legalmatter.security;

import com.java700.legalmatter.common.api.Problems;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Loads and authenticates local identity-provider accounts. */
@Service
public class LocalUserService {

    private final LocalUserRepository repository;
    private final LocalUserRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttempts;

    public LocalUserService(LocalUserRepository repository, LocalUserRoleRepository roleRepository,
                            PasswordEncoder passwordEncoder, LoginAttemptService loginAttempts) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttempts = loginAttempts;
    }

    public Optional<LocalUser> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    public LocalUser save(LocalUser user) {
        return repository.save(user);
    }

    public void saveRole(String userId, String roleName) {
        roleRepository.save(new LocalUserRole(userId, roleName));
    }

    public LocalUser authenticate(String username, String password) {
        LocalUser user = repository.findByUsername(username)
                .orElseThrow(() -> new Problems.NotFound("Invalid username or password"));
        if (!user.isEnabled()) {
            throw new Problems.NotFound("Invalid username or password");
        }
        if (loginAttempts.isLocked(user)) {
            throw new Problems.RateLimited("Account temporarily locked due to repeated failed attempts");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            loginAttempts.onFailure(user);
            throw new Problems.NotFound("Invalid username or password");
        }
        loginAttempts.onSuccess(user);
        return user;
    }

    public List<String> rolesOf(String userId) {
        return roleRepository.findByUserId(userId).stream()
                .map(LocalUserRole::getRoleName)
                .toList();
    }
}
