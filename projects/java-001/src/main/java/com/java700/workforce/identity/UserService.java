package com.java700.workforce.identity;

import com.java700.workforce.common.api.PageResponse;
import com.java700.workforce.common.api.Problems;
import com.java700.workforce.security.Roles;
import com.java700.workforce.security.SecurityUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserProfileRepository repository;

    public UserService(UserProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public UserProfile get(String id) {
        UserProfile user = repository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("User not found"));
        // least privilege: employees may only read their own record
        if (!SecurityUtil.hasRole(Roles.AUDITOR)
                && !SecurityUtil.hasRole(Roles.COMPLIANCE_ADMIN)
                && !SecurityUtil.hasRole(Roles.COMPLIANCE_OFFICER)
                && !SecurityUtil.hasRole(Roles.ACCESS_MANAGER)
                && !user.getId().equals(SecurityUtil.currentUserId())) {
            throw new Problems.NotFound("User not found");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserApi.UserView> search(String query, int page, int size) {
        var result = (query == null || query.isBlank())
                ? repository.findAll(PageRequest.of(page, size, Sort.by("username")))
                : repository.findByUsernameContainingIgnoreCase(
                        query, PageRequest.of(page, size, Sort.by("username")));
        return PageResponse.from(result.map(UserApi.UserView::from));
    }
}
