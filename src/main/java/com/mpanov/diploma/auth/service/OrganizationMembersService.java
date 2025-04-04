package com.mpanov.diploma.auth.service;

import com.mpanov.diploma.auth.dao.OrganizationDao;
import com.mpanov.diploma.auth.dao.OrganizationMemberDao;
import com.mpanov.diploma.auth.dao.ServiceUserDao;
import com.mpanov.diploma.auth.dto.organization.members.InviteMemberDto;
import com.mpanov.diploma.auth.dto.organization.members.UpdateMemberRolesDto;
import com.mpanov.diploma.auth.dto.user.UserSignupDto;
import com.mpanov.diploma.auth.exception.OrganizationActionNotAllowed;
import com.mpanov.diploma.auth.exception.common.NotFoundException;
import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.model.OrganizationMember;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.security.common.PasswordService;
import com.mpanov.diploma.data.MemberRole;
import com.mpanov.diploma.utils.EmailUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    public void updateMemberRoles(String organizationSlug, ServiceUser actorUser, UpdateMemberRolesDto dto, Long memberId) {
        log.info("updateMemberRoles: dto={}", dto);

        OrganizationMember actorMember = this.getActorMemberForOrganizationSlug(actorUser, organizationSlug);
        Set<MemberRole> actorMemberRoles = actorMember.getRoles();

        OrganizationMember member = organizationMemberDao.getOrganizationMemberByMemberId(memberId);
        Set<MemberRole> memberRoles = member.getRoles();

        Set<MemberRole> newRoles = dto.getNewRoles();

        if (newRoles.contains(MemberRole.ORGANIZATION_OWNER)) {
            throw new OrganizationActionNotAllowed("Cannot set ORGANIZATION_OWNER role to team member");
        }

        if (memberRoles.contains(MemberRole.ORGANIZATION_OWNER)) {
            throw new OrganizationActionNotAllowed("Cannot update roles of organization owner");
        }

        if (
                !actorMemberRoles.contains(MemberRole.ORGANIZATION_OWNER) &&
                        !memberRoles.contains(MemberRole.ORGANIZATION_ADMIN) &&
                        !memberRoles.contains(MemberRole.ORGANIZATION_MEMBERS_MANAGER)
        ) {
            throw new OrganizationActionNotAllowed("Actor member does not have any member management role");
        }

        Set<MemberRole> newRolesWithoutCurrent = this.subtract(newRoles, memberRoles);
        if (
                !actorMemberRoles.containsAll(newRolesWithoutCurrent) &&
                        !actorMemberRoles.contains(MemberRole.ORGANIZATION_OWNER) &&
                        !actorMemberRoles.contains(MemberRole.ORGANIZATION_ADMIN)
        ) {
            throw new OrganizationActionNotAllowed(
                    "Actor member "
                            + actorMember.getId()
                            + " does not have required roles to grant the following roles "
                            + newRoles
            );
        }

        organizationMemberDao.updateMemberWithNewRoles(member, newRoles);
    }

    private Set<MemberRole> subtract(Set<MemberRole> s1, Set<MemberRole> s2) {
        Set<MemberRole> rv = new HashSet<>(s1);
        rv.removeAll(s2);
        rv.remove(MemberRole.ORGANIZATION_MEMBER);
        return rv;
    }

    private OrganizationMember getActorMemberForOrganizationSlug(ServiceUser actorUser, String slug) {
        OrganizationMember member = null;
        for (OrganizationMember m : actorUser.getOrganizationMembers()) {
            if (StringUtils.equals(m.getOrganization().getSlug(), slug)) {
                member = m;
                break;
            }
        }
        if (member == null) {
            throw new NotFoundException(
                    OrganizationMember.class,
                    "organization.slug,memberUser.id",
                    slug + "," + actorUser.getId()
            );
        }
        return member;
    }

}
