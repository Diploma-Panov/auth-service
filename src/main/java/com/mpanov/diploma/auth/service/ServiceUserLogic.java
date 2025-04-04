package com.mpanov.diploma.auth.service;

import com.mpanov.diploma.auth.dao.ServiceUserDao;
import com.mpanov.diploma.auth.dto.common.TokenResponseDto;
import com.mpanov.diploma.auth.dto.user.UpdateUserInfoByAdminDto;
import com.mpanov.diploma.auth.dto.user.UpdateUserInfoDto;
import com.mpanov.diploma.auth.dto.user.UserSignupDto;
import com.mpanov.diploma.auth.exception.common.LoginException;
import com.mpanov.diploma.auth.exception.UserSignupException;
import com.mpanov.diploma.auth.model.*;
import com.mpanov.diploma.auth.model.common.LoginType;
import com.mpanov.diploma.auth.model.common.OrganizationType;
import com.mpanov.diploma.auth.model.common.UserSystemRole;
import com.mpanov.diploma.auth.security.*;
import com.mpanov.diploma.auth.security.common.JwtUserSubject;
import com.mpanov.diploma.auth.security.common.OrganizationAccessEntry;
import com.mpanov.diploma.auth.security.common.PasswordService;
import com.mpanov.diploma.data.MemberRole;
import com.mpanov.diploma.utils.EmailUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class ServiceUserLogic {

    private final PasswordService passwordService;

    private final ServiceUserDao serviceUserDao;

    private final JwtPayloadService jwtPayloadService;

    private final ImageService imageService;

    public TokenResponseDto signupNewUser(UserSignupDto dto) {
        ServiceUser user = this.signupNewUserInternal(dto);

        // Create auth subject for new user
        JwtUserSubject subject = JwtUserSubject.builder()
                .userId(user.getId())
                .username(user.getEmail())
                .userSystemRole(user.getSystemRole())
                .loginType(LoginType.USER_LOGIN)
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .organizations(this.mapOrganizationAccessEntries(user))
                .build();

        return jwtPayloadService.getTokensForUserSubject(subject);
    }

    public ServiceUser signupNewUserInternal(UserSignupDto dto) {
        log.info("signupNewUser: username={}, name={}", dto.getUsername(), dto.getFirstName() + " " + dto.getLastName());
        passwordService.assertPasswordCompliant(dto.getPassword());
        String passwordHash = passwordService.encryptPassword(dto.getPassword());
        String normalizedEmail = EmailUtils.normalizeEmail(dto.getUsername());

        // Create new permanent organization
        Organization permanentOrganization = Organization.builder()
                .name(dto.getFirstName() + "'s Organization")
                .slug(generateSlugFromEmail(normalizedEmail))
                .organizationScope(dto.getRegistrationScope())
                .siteUrl(dto.getSiteUrl())
                .description(dto.getFirstName() + "'s Personal Organization")
                .type(OrganizationType.PERMANENT)
                .build();

        // Create new permanent organization member
        OrganizationMember member = OrganizationMember.builder()
                .memberUrls(new Long[0])
                .allowedAllUrls(true)
                .roles(Set.of(MemberRole.ORGANIZATION_OWNER))
                .build();

        // Create new service user
        ServiceUser userToCreate = ServiceUser.builder()
                .firstname(dto.getFirstName())
                .lastname(dto.getLastName())
                .companyName(dto.getCompanyName())
                .email(normalizedEmail)
                .passwordHash(passwordHash)
                .systemRole(UserSystemRole.USER)
                .build();

        // Save new user with all relations
        ServiceUser user = serviceUserDao.createServiceUser(
                userToCreate,
                permanentOrganization,
                member
        );

        String pictureBase64 = dto.getProfilePictureBase64();
        if (StringUtils.isNotBlank(pictureBase64)) {
            byte[] profilePictureBytes = Base64.getDecoder().decode(pictureBase64.getBytes(StandardCharsets.UTF_8));
            String profilePictureUrl = imageService.saveUserProfilePicture(user.getId(), profilePictureBytes);
            user = serviceUserDao.updateWithProfilePictureUrl(user, profilePictureUrl);
        }

        return user;
    }

    public UserAuthentication login(String username, String password) {
        log.info("login: attempting for username {}", username);
        ServiceUser user = serviceUserDao.getServiceUserByEmailThrowable(username);
        if (!passwordService.passwordMatches(password, user.getPasswordHash())) {
            throw new LoginException("Incorrect password");
        }
        JwtUserSubject subject = this.buildSubjectForUser(user, LoginType.USER_LOGIN);

        serviceUserDao.updateLoginDate(user.getId());

        return new UserAuthentication(subject);
    }

    public JwtUserSubject loginWithUserIdBySystem(Long userId) {
        log.info("loginWithUserIdBySystem: for userId={}", userId);
        ServiceUser user = serviceUserDao.getServiceUserByIdThrowable(userId);
        return this.buildSubjectForUser(user, LoginType.SYSTEM_LOGIN);
    }

    public JwtUserSubject refreshSubject(JwtUserSubject subject) {
        log.info("refreshSubject: for userId={}", subject.getUserId());
        Long userId = subject.getUserId();

        ServiceUser user = serviceUserDao.getServiceUserByIdThrowable(userId);

        return this.buildSubjectForUser(user, LoginType.USER_LOGIN);
    }

    public void changeUserSystemRole(Long userId, UserSystemRole newRole) {
        log.info("changeUserSystemRole: for userId={}, newRole={}", userId, newRole);
        serviceUserDao.updateUserSystemRole(userId, newRole);
    }

    public ServiceUser updateUserInfo(ServiceUser user, UpdateUserInfoDto dto) {
        log.info("updateUserInfo: for userId={}, dto={}", user.getId(), dto);

        if (StringUtils.isNotBlank(dto.getNewFirstname())) {
            user.setFirstname(dto.getNewFirstname());
        }

        if (StringUtils.isNotBlank(dto.getNewLastname())) {
            user.setLastname(dto.getNewLastname());
        }

        if (StringUtils.isNotBlank(dto.getNewEmail())) {
            user.setEmail(dto.getNewEmail());
        }

        user.setCompanyName(dto.getNewCompanyName());

        return serviceUserDao.syncInfo(user);
    }

    public ServiceUser updateUserInfoByAdmin(Long userId, UpdateUserInfoByAdminDto dto) {
        log.info("updateUserInfoByAdmin: for userId={}, dto={}", userId, dto);

        ServiceUser user = serviceUserDao.getServiceUserByIdThrowable(userId);

        if (StringUtils.isNotBlank(dto.getNewFirstname())) {
            user.setFirstname(dto.getNewFirstname());
        }

        if (StringUtils.isNotBlank(dto.getNewLastname())) {
            user.setLastname(dto.getNewLastname());
        }

        if (dto.getNewRole() != null) {
            user.setSystemRole(dto.getNewRole());
        }

        if (StringUtils.isNotBlank(dto.getNewEmail())) {
            user.setEmail(dto.getNewEmail());
        }

        user.setCompanyName(dto.getNewCompanyName());

        return serviceUserDao.syncInfo(user);
    }

    private JwtUserSubject buildSubjectForUser(ServiceUser user, LoginType loginType) {
        return JwtUserSubject.builder()
                .userId(user.getId())
                .username(user.getEmail())
                .userSystemRole(user.getSystemRole())
                .loginType(loginType)
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .organizations(this.mapOrganizationAccessEntries(user))
                .build();
    }

    private Set<OrganizationAccessEntry> mapOrganizationAccessEntries(ServiceUser user) {
        Set<OrganizationAccessEntry> rv = new HashSet<>();
        Set<OrganizationMember> members = user.getOrganizationMembers();
        for (OrganizationMember member : members) {
            rv.add(
                    OrganizationAccessEntry.builder()
                            .organizationId(member.getOrganization().getId())
                            .slug(member.getOrganization().getSlug())
                            .allowedUrls(member.getMemberUrls())
                            .allowedAllUrls(member.getAllowedAllUrls())
                            .roles(member.getRoles())
                            .build()
            );
        }

        return rv;
    }

    private String generateSlugFromEmail(String email) {
        if (StringUtils.isBlank(email)) {
            throw new UserSignupException(SignupErrorType.INVALID_EMAIL_FORMAT, "Email %s is invalid".formatted(email));
        }
        return email.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");
    }

}
