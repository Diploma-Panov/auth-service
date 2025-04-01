package com.mpanov.diploma.auth.dao;

import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.model.OrganizationMember;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.repository.OrganizationMemberRepository;
import com.mpanov.diploma.auth.repository.OrganizationRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class OrganizationDao {

    private final OrganizationRepository organizationRepository;

    private final OrganizationMemberRepository organizationMemberRepository;

    public Page<Organization> getAllOrganizationsByMemberUserId(Long memberUserId, Pageable pageable) {
        Set<Long> organizationIds = organizationMemberRepository
                .findAllOrganizationIdsByMemberUserId(memberUserId);
        return organizationRepository.findAllByIds(
                organizationIds,
                pageable
        );
    }

    public int countAllOrganizationsByMemberUserId(Long memberUserId) {
        return organizationMemberRepository.countAllOrganizationIdsByMemberUserId(memberUserId);
    }

    public Optional<Organization> findOrganizationBySlugOptional(String slug) {
        return organizationRepository.findOrganizationBySlug(slug);
    }

    public Organization createOrganizationForUser(ServiceUser user, Organization organizationToCreate, OrganizationMember ownerMemberToCreate) {
        user.addOrganizationMember(ownerMemberToCreate);
        user.addOrganization(organizationToCreate);
        organizationToCreate.addMember(ownerMemberToCreate);
        return organizationRepository.save(organizationToCreate);
    }

    public Organization updateWithAvatarUrl(Organization organization, String avatarUrl) {
        organization.setOrganizationAvatarUrl(StringUtils.isBlank(avatarUrl) ? null : avatarUrl);
        return organizationRepository.save(organization);
    }

    public Organization removeAvatar(Organization organization) {
        organization.setOrganizationAvatarUrl(null);
        return organizationRepository.save(organization);
    }

    public boolean existsBySlug(String slug) {
        return organizationRepository.existsOrganizationBySlug(slug);
    }

    public Organization syncOrganization(Organization organization) {
        return organizationRepository.save(organization);
    }
}
