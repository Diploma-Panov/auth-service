package com.mpanov.diploma.auth.service;

import com.mpanov.diploma.auth.dao.OrganizationDao;
import com.mpanov.diploma.auth.dao.OrganizationMemberDao;
import com.mpanov.diploma.auth.dao.ServiceUserDao;
import com.mpanov.diploma.auth.dto.organization.members.InviteMemberDto;
import com.mpanov.diploma.auth.dto.user.UserSignupDto;
import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.model.OrganizationMember;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.security.common.PasswordService;
import com.mpanov.diploma.utils.EmailUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class OrganizationMembersService {

    private final OrganizationMemberDao organizationMemberDao;

    private final OrganizationDao organizationDao;

    private final ServiceUserDao serviceUserDao;

    private final ServiceUserLogic serviceUserLogic;

    private final PasswordService passwordService;

    public List<OrganizationMember> getOrganizationMembersBySlug(String slug, Pageable pageable) {
        log.info("getOrganizationMembersBySlug: slug={}, pageable={}", slug, pageable);
        Page<OrganizationMember> members = organizationMemberDao.getOrganizationMembersBySlugPageable(slug, pageable);
        return members.getContent();
    }

    public int countOrganizationMembersBySlug(String slug) {
        log.info("countOrganizationMembersBySlug: for slug={}", slug);
        return organizationMemberDao.countOrganizationMembersBySlug(slug);
    }

    public void inviteNewOrganizationMember(String slug, InviteMemberDto dto) {
        log.info("inviteNewOrganizationMember: slug={}, dto={}", slug, dto);

        String normalizedEmail = EmailUtils.normalizeEmail(dto.getEmail());

        organizationMemberDao.assertMemberDoesNotExistByEmailAndSlug(normalizedEmail, slug);

        Organization organization = organizationDao.findOrganizationBySlugThrowable(slug);
        Optional<ServiceUser> userOpt = serviceUserDao.getServiceUserByEmailOptional(normalizedEmail);

        ServiceUser user;
        if (userOpt.isPresent()) {
            log.info("inviteNewOrganizationMember: successfully found user with email={}", normalizedEmail);
            user = userOpt.get();
        } else {
            log.info("inviteNewOrganizationMember: user with email={} not found, implicitly creating", normalizedEmail);
            UserSignupDto signupDto = UserSignupDto.builder()
                    .username(normalizedEmail)
                    .password(passwordService.generateCompliantPassword())
                    .firstName(dto.getFirstname())
                    .lastName(dto.getLastname())
                    .registrationScope(organization.getOrganizationScope())
                    .build();
            user = serviceUserLogic.signupNewUserInternal(signupDto);
        }

        OrganizationMember member = OrganizationMember.builder()
                .displayFirstname(dto.getFirstname())
                .displayLastname(dto.getLastname())
                .roles(dto.getRoles())
                .memberUrls(dto.getAllowedUrls())
                .allowedAllUrls(dto.getAllowedAllUrls())
                .build();

        organizationMemberDao.createNewMember(organization, user, member);
    }

}
