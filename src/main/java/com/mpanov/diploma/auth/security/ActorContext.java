package com.mpanov.diploma.auth.security;

import com.mpanov.diploma.auth.dao.OrganizationMemberDao;
import com.mpanov.diploma.auth.dao.ServiceUserDao;
import com.mpanov.diploma.auth.model.ServiceUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ActorContext {

    private final ServiceUserDao serviceUserDao;

    private final OrganizationMemberDao organizationMemberDao;

    public ServiceUser getAuthenticatedUser() {
        JwtUserSubject subject = (JwtUserSubject) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        Long userId = subject.getUserId();
        log.debug("getAuthenticatedUser: looking up for user with id={}", userId);
        return serviceUserDao.findServiceUserByIdThrowable(userId);
    }

    public void assertHasAccessToOrganization(Long userId, String organizationSlug) {
        boolean exists = organizationMemberDao.existsByMemberUserIdAndOrganizationSlug(userId, organizationSlug);
        if (!exists) {
            log.warn("User {} does not have access to organization {}", userId, organizationSlug);
            throw new AuthorizationDeniedException("User " + userId + " does not have access to organization " + organizationSlug);
        }
    }

}
