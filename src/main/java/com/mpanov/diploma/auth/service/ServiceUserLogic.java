package com.mpanov.diploma.auth.service;

import com.mpanov.diploma.auth.dao.ServiceUserDao;
import com.mpanov.diploma.auth.dto.user.UpdateUserInfoByAdminDto;
import com.mpanov.diploma.auth.dto.user.UpdateUserInfoDto;
import com.mpanov.diploma.auth.dto.user.UserSignupDto;
import com.mpanov.diploma.auth.exception.LoginException;
import com.mpanov.diploma.auth.exception.ShortCodeExpiredException;
import com.mpanov.diploma.auth.exception.UserSignupException;
import com.mpanov.diploma.auth.kafka.UserUpdatesKafkaProducer;
import com.mpanov.diploma.auth.model.*;
import com.mpanov.diploma.auth.security.*;
import com.mpanov.diploma.data.*;
import com.mpanov.diploma.data.dto.TokenResponseDto;
import com.mpanov.diploma.data.exception.NonCompliantPasswordException;
import com.mpanov.diploma.data.security.JwtUserSubject;
import com.mpanov.diploma.data.security.OrganizationAccessEntry;
import com.mpanov.diploma.data.security.PasswordService;
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
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class ServiceUserLogic {

    private final PasswordService passwordService;

    private final ServiceUserDao serviceUserDao;

    private final JwtPayloadService jwtPayloadService;

    private final ImageService imageService;

    private final CacheService cacheService;

    private final UserUpdatesKafkaProducer userUpdatesKafkaProducer;

    public ServiceUser getServiceUserByIdThrowable(Long id) {
        return serviceUserDao.getServiceUserByIdThrowable(id);
    }

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
        try {
            passwordService.assertPasswordCompliant(dto.getPassword());
        } catch (NonCompliantPasswordException e) {
            throw new UserSignupException(SignupErrorType.NON_COMPLIANT_PASSWORD, "Password " + dto.getPassword() + " is not compliant");
        }
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

        userUpdatesKafkaProducer.sendUserUpdateAsync(user.getId());

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

    public String loginAsUserByAdmin(Long userId) {
        log.info("loginAsUserByAdmin: userId={}", userId);

        JwtUserSubject subject = this.loginWithUserIdBySystem(userId);
        String accessToken = jwtPayloadService.getTokensForUserSubject(subject)
                .getAccessToken();
        log.debug("loginAsUserByAdmin: generated accessToken for userId={}", userId);

        String shortCode = UUID.randomUUID().toString();
        cacheService.cacheWithTTL(shortCode, accessToken);
        log.debug("loginAsUserByAdmin: generated and cached saved shortCode={}", shortCode);

        return shortCode;
    }

    public TokenResponseDto loginAsUserBySystem(Long userId) {
        log.info("loginAsUserBySystem: userId={}", userId);
        ServiceUser user = serviceUserDao.getServiceUserByIdThrowable(userId);
        JwtUserSubject subject = this.buildSubjectForUser(user, LoginType.USER_LOGIN);
        TokenResponseDto tokenResponseDto = jwtPayloadService.getTokensForUserSubject(subject);
        log.debug("loginAsUserBySystem: generated accessToken for userId={}", userId);
        return tokenResponseDto;
    }

    public TokenResponseDto exchangeShortCode(String shortCode) {
        log.info("exchangeShortCode: shortCode={}", shortCode);
        String accessToken = cacheService.getValue(shortCode);
        if (accessToken == null) {
            throw new ShortCodeExpiredException("ShortCode " + shortCode + " expired");
        }
        return new TokenResponseDto(accessToken, null);
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

        ServiceUser updatedUser = serviceUserDao.syncInfo(user);

        userUpdatesKafkaProducer.sendUserUpdateAsync(user.getId());

        return updatedUser;
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

        ServiceUser updatedUser = serviceUserDao.syncInfo(user);

        userUpdatesKafkaProducer.sendUserUpdateAsync(user.getId());

        return updatedUser;
    }

    public ServiceUser updateProfilePicture(ServiceUser user, String newProfilePictureBase64) {
        Long userId = user.getId();
        log.info("updateProfilePicture: for userId={}", userId);
        byte[] profilePictureBytes = Base64.getDecoder().decode(newProfilePictureBase64.getBytes(StandardCharsets.UTF_8));
        String profilePictureUrl = imageService.saveUserProfilePicture(userId, profilePictureBytes);
        return serviceUserDao.updateWithProfilePictureUrl(user, profilePictureUrl);
    }

    public ServiceUser removeProfilePicture(ServiceUser user) {
        Long userId = user.getId();
        log.info("removeProfilePicture: for userId={}", userId);
        imageService.removeUserProfilePicture(userId, user.getProfilePictureUrl());
        return serviceUserDao.updateWithProfilePictureUrl(user, null);
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
