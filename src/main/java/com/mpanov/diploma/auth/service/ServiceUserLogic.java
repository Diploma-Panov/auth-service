package com.mpanov.diploma.auth.service;

import com.mpanov.diploma.MemberRole;
import com.mpanov.diploma.auth.dao.ServiceUserDao;
import com.mpanov.diploma.auth.dto.TokenResponseDto;
import com.mpanov.diploma.auth.dto.UserSignupDto;
import com.mpanov.diploma.auth.exception.LoginException;
import com.mpanov.diploma.auth.model.*;
import com.mpanov.diploma.auth.security.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class ServiceUserLogic {

    private final PasswordService passwordService;

    private final ServiceUserDao serviceUserDao;

    private final JwtPayloadService jwtPayloadService;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TokenResponseDto signupNewUser(UserSignupDto dto) {
        passwordService.assertPasswordCompliant(dto.getPassword());
        String passwordHash = passwordService.encryptPassword(dto.getPassword());
        String normalizedEmail = StringUtils.normalizeSpace(dto.getUsername()).toLowerCase();

        // Create new permanent organization
        Organization permanentOrganization = Organization.builder()
                .organizationName(dto.getFirstName() + "'s Organization")
                .type(OrganizationType.PERMANENT)
                .build();

        // Create new permanent organization member
        OrganizationMember member = OrganizationMember.builder()
                .allowedUrls(new Long[0])
                .allowedAllUrls(true)
                .roles(Set.of(MemberRole.ORGANIZATION_OWNER))
                .build();

        // Create new service user
        ServiceUser userToCreate = ServiceUser.builder()
                .firstname(dto.getFirstName())
                .lastname(dto.getLastName())
                .email(normalizedEmail)
                .passwordHash(passwordHash)
                .phone(dto.getPhone())
                .type(UserType.USER)
                .build();

        // Save new user with all relations
        ServiceUser user = serviceUserDao.createServiceUser(
                userToCreate,
                permanentOrganization,
                member
        );

        // Create auth subject for new user
        JwtUserSubject subject = JwtUserSubject.builder()
                .userId(user.getId())
                .username(user.getEmail())
                .userType(user.getType())
                .loginType(LoginType.USER_LOGIN)
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .organizations(this.mapOrganizationAccessEntries(user))
                .build();

        return jwtPayloadService.getTokensForUserSubject(subject);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public UserAuthentication login(String username, String password) {
        ServiceUser user = serviceUserDao.findServiceUserByEmailThrowable(username);
        if (!passwordService.passwordMatches(password, user.getPasswordHash())) {
            throw new LoginException("Incorrect password");
        }
        JwtUserSubject subject = JwtUserSubject.builder()
                .userId(user.getId())
                .username(user.getEmail())
                .userType(user.getType())
                .loginType(LoginType.USER_LOGIN)
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .organizations(this.mapOrganizationAccessEntries(user))
                .build();
        return new UserAuthentication(subject);
    }

    private Set<OrganizationAccessEntry> mapOrganizationAccessEntries(ServiceUser user) {
        Set<OrganizationAccessEntry> rv = new HashSet<>();
        Set<OrganizationMember> members = user.getOrganizationMembers();
        for (OrganizationMember member : members) {
            rv.add(
                    OrganizationAccessEntry.builder()
                            .organizationId(member.getOrganization().getId())
                            .allowedUrls(member.getAllowedUrls())
                            .allowedAllUrls(member.getAllowedAllUrls())
                            .roles(member.getRoles())
                            .build()
            );
        }

        return rv;
    }

}
