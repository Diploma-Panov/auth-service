package com.mpanov.diploma.auth.security;

import com.mpanov.diploma.auth.dao.ServiceUserDao;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.data.MemberPermission;
import com.mpanov.diploma.data.MemberRole;
import com.mpanov.diploma.data.security.JwtUserSubject;
import com.mpanov.diploma.data.security.OrganizationAccessEntry;
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

    public JwtUserSubject getJwtUserSubject() {
        return (JwtUserSubject) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public ServiceUser getAuthenticatedUser() {
        JwtUserSubject subject = this.getJwtUserSubject();
        Long userId = subject.getUserId();
        log.debug("getAuthenticatedUser: looking up for user with id={}", userId);
        return serviceUserDao.getServiceUserByIdThrowable(userId);
    }

    public void assertHasAccessToOrganization(String organizationSlug, MemberPermission permission) {
        JwtUserSubject subject = this.getJwtUserSubject();

        Long userId = subject.getUserId();

        Set<OrganizationAccessEntry> organizations = subject.getOrganizations();
        OrganizationAccessEntry targetOrganization = organizations.stream()
                .filter(organization -> organizationSlug.equals(organization.getSlug()))
                .findFirst()
                .orElseThrow(() ->
                        new AuthorizationDeniedException("User " + userId + " does not have access to organization " + organizationSlug)
                );

        Set<MemberRole> roles = targetOrganization.getRoles();
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
