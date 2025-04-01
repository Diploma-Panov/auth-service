package com.mpanov.diploma.auth.dao;

import com.mpanov.diploma.auth.exception.common.NotFoundException;
import com.mpanov.diploma.auth.model.OrganizationMember;
import com.mpanov.diploma.auth.repository.OrganizationMemberRepository;
import lombok.AllArgsConstructor;
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

}
