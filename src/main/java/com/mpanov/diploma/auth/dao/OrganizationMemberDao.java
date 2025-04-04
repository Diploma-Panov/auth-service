package com.mpanov.diploma.auth.dao;

import com.mpanov.diploma.auth.exception.common.NotFoundException;
import com.mpanov.diploma.auth.model.OrganizationMember;
import com.mpanov.diploma.auth.repository.OrganizationMemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrganizationMemberDao {

    private final OrganizationMemberRepository organizationMemberRepository;

    public boolean existsByMemberUserIdAndOrganizationSlug(Long memberUserId, String organizationSlug) {
        return organizationMemberRepository.existsByMemberUserIdAndOrganizationSlug(memberUserId, organizationSlug);
    }

    public OrganizationMember getOrganizationMemberByMemberUserIdAndOrganizationSlugThrowable(Long memberUserId, String organizationSlug) {
        return organizationMemberRepository.findByMemberUserIdAndOrganizationSlug(memberUserId, organizationSlug)
                .orElseThrow(() -> new NotFoundException(OrganizationMember.class, "memberUserId,organizationSlug", memberUserId + "," + organizationSlug));
    }

    public Page<OrganizationMember> getOrganizationMembersBySlugPageable(String slug, Pageable pageable) {
        return organizationMemberRepository.findMembersByOrganizationSlug(slug, pageable);
    }

    public int countOrganizationMembersBySlug(String slug) {
        return organizationMemberRepository.countAllByOrganizationSlug(slug);
    }

}
