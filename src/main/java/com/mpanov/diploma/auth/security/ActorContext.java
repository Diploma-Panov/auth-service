package com.mpanov.diploma.auth.security;

import com.mpanov.diploma.auth.dao.OrganizationMemberDao;
import com.mpanov.diploma.auth.dao.ServiceUserDao;
import com.mpanov.diploma.auth.model.OrganizationMember;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.data.MemberPermission;
import com.mpanov.diploma.data.MemberRole;
import com.mpanov.diploma.data.security.JwtUserSubject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;

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
        return serviceUserDao.getServiceUserByIdThrowable(userId);
    }

    public void assertHasAccessToOrganization(String organizationSlug, MemberPermission permission) {
        Long userId = this.getAuthenticatedUser().getId();
        boolean exists = organizationMemberDao.existsByMemberUserIdAndOrganizationSlug(userId, organizationSlug);
        if (!exists) {
            log.warn("User {} does not have access to organization {}", userId, organizationSlug);
            throw new AuthorizationDeniedException("User " + userId + " does not have access to organization " + organizationSlug);
        }

        OrganizationMember member = organizationMemberDao.getOrganizationMemberByMemberUserIdAndOrganizationSlugThrowable(userId, organizationSlug);
        Set<MemberRole> roles = member.getRoles();
        for (MemberRole role : roles) {
            if (role.getPermissions().contains(permission)) {
                return;
            }
        }

        throw new AuthorizationDeniedException(
                "User " + userId + " does not have required permission " + permission + " for organization " + organizationSlug
        );
    }

}
