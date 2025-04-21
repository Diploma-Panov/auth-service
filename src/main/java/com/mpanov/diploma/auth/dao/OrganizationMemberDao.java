package com.mpanov.diploma.auth.dao;

import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.model.OrganizationMember;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.repository.OrganizationMemberRepository;
import com.mpanov.diploma.data.MemberRole;
import com.mpanov.diploma.data.exception.DuplicateException;
import com.mpanov.diploma.data.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class OrganizationMemberDao {

    private final OrganizationMemberRepository organizationMemberRepository;

    public List<OrganizationMember> getOrganizationMembersByUserId(Long userId) {
        return organizationMemberRepository.findAllByMemberUserId(userId);
    }

    public boolean existsByMemberUserIdAndOrganizationSlug(Long memberUserId, String organizationSlug) {
        return organizationMemberRepository.existsByMemberUserIdAndOrganizationSlug(memberUserId, organizationSlug);
    }

    public OrganizationMember getOrganizationMemberByMemberUserIdAndOrganizationSlugThrowable(Long memberUserId, String organizationSlug) {
        return organizationMemberRepository.findByMemberUserIdAndOrganizationSlug(memberUserId, organizationSlug)
                .orElseThrow(() -> new NotFoundException(OrganizationMember.class, "memberUserId,organizationSlug", memberUserId + "," + organizationSlug));
    }

    public OrganizationMember getOrganizationMemberByIdThrowable(Long memberId) {
        return organizationMemberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(OrganizationMember.class, "id", memberId.toString()));
    }

    public Page<OrganizationMember> getOrganizationMembersBySlugPageable(String slug, Pageable pageable) {
        return organizationMemberRepository.findMembersByOrganizationSlug(slug, pageable);
    }

    public int countOrganizationMembersBySlug(String slug) {
        return organizationMemberRepository.countAllByOrganizationSlug(slug);
    }

    public OrganizationMember createNewMember(Organization organization, ServiceUser serviceUser, OrganizationMember member) {
        organization.addMember(member);
        serviceUser.addOrganizationMember(member);
        return organizationMemberRepository.save(member);
    }

    public void updateMemberWithNewRoles(OrganizationMember members, Set<MemberRole> newRoles) {
        members.setRoles(newRoles);
        organizationMemberRepository.save(members);
    }

    public void assertMemberDoesNotExistByEmailAndSlug(String email, String slug) {
        boolean exists = organizationMemberRepository.existsByMemberUserEmailAndOrganizationSlug(email, slug);
        if (exists) {
            throw new DuplicateException(OrganizationMember.class, "memberUser.email,organization.slug", email + "," + slug);
        }
    }

    public void updateMemberWithUrls(OrganizationMember member, Set<Long> urlsIds, boolean allowedAllUrls) {
        Long[] urlsIdArray = urlsIds.toArray(new Long[0]);
        member.setMemberUrls(urlsIdArray);
        member.setAllowedAllUrls(allowedAllUrls);
        organizationMemberRepository.save(member);
    }

    public void deleteMember(OrganizationMember member) {
        member.getOrganization().removeMember(member);
        member.getMemberUser().removeMember(member);
        organizationMemberRepository.delete(member);
    }
}
