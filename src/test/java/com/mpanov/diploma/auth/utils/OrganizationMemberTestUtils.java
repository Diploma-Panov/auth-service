package com.mpanov.diploma.auth.utils;

import com.mpanov.diploma.auth.dao.OrganizationDao;
import com.mpanov.diploma.auth.dto.organization.members.InviteMemberDto;
import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.model.OrganizationMember;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.service.OrganizationMembersService;
import com.mpanov.diploma.data.MemberRole;
import com.mpanov.diploma.utils.RandomUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrganizationMemberTestUtils {
    
    private final OrganizationDao organizationDao;

    private final OrganizationMembersService organizationMembersService;

    private final CommonTestUtils commonTestUtils;

    public ImmutablePair<ServiceUser, OrganizationMember> inviteMemberInOrganization(Organization organization) {
        InviteMemberDto dto = InviteMemberDto.builder()
                .firstname(RandomUtils.generateRandomAlphabeticalString(20))
                .lastname(RandomUtils.generateRandomAlphabeticalString(20))
                .email(commonTestUtils.generateRandomEmail())
                .allowedAllUrls(false)
                .allowedUrls(new Long[] {1L, 2L, 10L})
                .roles(Set.of(MemberRole.ORGANIZATION_MEMBER, MemberRole.ORGANIZATION_URLS_MANAGER))
                .build();
        OrganizationMember organizationMember = organizationMembersService.inviteNewOrganizationMember(organization.getSlug(), dto);
        return ImmutablePair.of(organizationMember.getMemberUser(), organizationMember);
    }
    
}
