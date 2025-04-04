package com.mpanov.diploma.auth.service;

import com.mpanov.diploma.auth.dao.OrganizationDao;
import com.mpanov.diploma.auth.dto.organization.CreateOrganizationDto;
import com.mpanov.diploma.auth.dto.organization.UpdateOrganizationInfoDto;
import com.mpanov.diploma.auth.exception.OrganizationActionNotAllowed;
import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.model.OrganizationMember;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.data.MemberRole;
import com.mpanov.diploma.data.OrganizationType;
import com.mpanov.diploma.data.exception.DuplicateException;
import com.mpanov.diploma.data.exception.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class OrganizationService {

    private final OrganizationDao organizationDao;

    private final ImageService imageService;

    public List<Organization> getUserOrganizations(ServiceUser user, Pageable pageable) {
        Long userId = user.getId();
        log.info("getUserOrganizations: for userId={}", userId);
        Page<Organization> rawData = organizationDao.getAllOrganizationsByMemberUserId(
                user.getId(), pageable
        );

        return rawData.getContent();
    }

    public int countUserOrganizations(ServiceUser user) {
        Long userId = user.getId();
        log.info("countUserOrganizations: for userId={}", userId);
        return organizationDao.countAllOrganizationsByMemberUserId(userId);
    }

    public Organization getOrganizationBySlugThrowable(String slug) {
        log.info("getOrganizationBySlug: for slug={}", slug);
        return organizationDao.findOrganizationBySlugOptional(slug)
                .orElseThrow(() -> new NotFoundException(Organization.class, "slug", slug));
    }

    public void createOrganizationByUser(ServiceUser user, CreateOrganizationDto dto) {
        log.info("createOrganizationByUser: for user={}, organizationName={}, slug={}", user.getId(), dto.getName(), dto.getSlug());

        String slug = dto.getSlug();
        assertSlugIsUnique(slug);

        Organization organizationToCreate = Organization.builder()
                .name(dto.getName())
                .slug(slug)
                .organizationScope(dto.getScope())
                .siteUrl(dto.getUrl())
                .description(dto.getDescription())
                .type(OrganizationType.MANUAL)
                .build();

        OrganizationMember ownerMemberToCreate = OrganizationMember.builder()
                .memberUrls(new Long[0])
                .allowedAllUrls(true)
                .roles(Set.of(MemberRole.ORGANIZATION_OWNER))
                .build();

        Organization createdOrganization = organizationDao.createOrganizationForUser(
                user,
                organizationToCreate,
                ownerMemberToCreate
        );

        String avatarBase64 = dto.getAvatarBase64();
        if (StringUtils.isBlank(avatarBase64)) {
            return;
        }

        Long createdOrganizationId = createdOrganization.getId();
        log.info("createOrganizationByUser: saving avatar for organizationId={}", createdOrganizationId);
        byte[] avatarBytes = Base64.getDecoder().decode(dto.getAvatarBase64().getBytes(StandardCharsets.UTF_8));
        String avatarUrl = imageService.saveOrganizationAvatar(avatarBytes, createdOrganizationId);

        organizationDao.updateWithAvatarUrl(createdOrganization, avatarUrl);
    }

    private void assertSlugIsUnique(String slug) {
        log.info("assertSlugIsUnique: for slug={}", slug);
        if (organizationDao.existsBySlug(slug)) {
            throw new DuplicateException(Organization.class, "slug", slug);
        }
    }

    public Organization patchOrganizationWithPartialData(String organizationSlug, UpdateOrganizationInfoDto partialData) {
        log.info("patchOrganizationWithPartialData: for organizationSlug={}, with partialData={}", organizationSlug, partialData);
        Organization organization = this.getOrganizationBySlugThrowable(organizationSlug);

        String newSlug = partialData.getNewSlug();
        String newName = partialData.getNewName();
        String newDescription = partialData.getNewDescription();
        String newUrl = partialData.getNewUrl();

        if (newSlug != null) {
            if (!StringUtils.equals(organizationSlug, newSlug)) {
                assertSlugIsUnique(partialData.getNewSlug());
            }
            organization.setSlug(newSlug);
        }

        if (newName != null) {
            organization.setName(newName);
        }

        if (newDescription != null) {
            organization.setDescription(newDescription);
        }

        if (newUrl != null) {
            organization.setSiteUrl(newUrl);
        }

        return organizationDao.syncOrganization(organization);
    }

    public Organization updateOrganizationAvatar(String organizationSlug, String newAvatarBase64) {
        log.info("updateOrganizationAvatar: for slug={}", organizationSlug);
        Organization organization = this.getOrganizationBySlugThrowable(organizationSlug);
        byte[] newAvatarBytes = Base64.getDecoder().decode(newAvatarBase64.getBytes(StandardCharsets.UTF_8));
        String newAvatarUrl = imageService.saveOrganizationAvatar(newAvatarBytes, organization.getId());
        return organizationDao.updateWithAvatarUrl(organization, newAvatarUrl);
    }

    public Organization removeOrganizationAvatar(String organizationSlug) {
        log.info("removeOrganizationAvatar: for slug={}", organizationSlug);
        Organization organization = this.getOrganizationBySlugThrowable(organizationSlug);
        imageService.removeOrganizationAvatar(organization.getId(), organization.getOrganizationAvatarUrl());
        return organizationDao.removeAvatar(organization);
    }

    public void removeOrganization(String slug) {
        log.info("removeOrganization: for slug={}", slug);
        Organization organization = this.getOrganizationBySlugThrowable(slug);
        if (organization.getType() == OrganizationType.PERMANENT) {
            throw new OrganizationActionNotAllowed("Cannot remove a permanent organization " + slug);
        }
        organizationDao.removeOrganization(organization);
    }
}
