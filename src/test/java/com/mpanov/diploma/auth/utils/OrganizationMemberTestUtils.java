package com.mpanov.diploma.auth.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static com.mpanov.diploma.auth.config.SecurityConfig.API_USER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Service
@RequiredArgsConstructor
public class OrganizationMemberTestUtils {
    
    private final OrganizationMembersService organizationMembersService;

    private final CommonTestUtils commonTestUtils;

    private final ObjectMapper objectMapper;

    private final MockMvc mockMvc;

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

    public ImmutablePair<ServiceUser, OrganizationMember> inviteMemberInOrganization(Organization organization, ServiceUser user, Set<MemberRole> roles) {
        InviteMemberDto dto = InviteMemberDto.builder()
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .allowedAllUrls(false)
                .allowedUrls(new Long[] {1L, 2L, 10L})
                .roles(roles)
                .build();
        OrganizationMember organizationMember = organizationMembersService.inviteNewOrganizationMember(organization.getSlug(), dto);
        return ImmutablePair.of(organizationMember.getMemberUser(), organizationMember);
    }

    public Long getMemberIdByUserAndOrganization(ServiceUser user, String orgSlug) {
        return user.getOrganizationMembers().stream()
                .filter(m -> m.getOrganization().getSlug().equals(orgSlug))
                .map(OrganizationMember::getId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Member not found for organization " + orgSlug));
    }

    public void inviteMemberWithEmail(String orgSlug, String accessToken, String email) throws Exception {
        InviteMemberDto inviteDto = InviteMemberDto.builder()
                .firstname("Test")
                .lastname("User")
                .email(email)
                .allowedAllUrls(false)
                .allowedUrls(new Long[]{1L})
                .roles(Set.of(MemberRole.ORGANIZATION_MEMBER))
                .build();
        String body = objectMapper.writeValueAsString(inviteDto);
        mockMvc.perform(post(API_USER + "/organizations/" + orgSlug + "/members")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    public void inviteMemberWithName(String orgSlug, String accessToken, String firstName, String lastName, String email) throws Exception {
        InviteMemberDto inviteDto = InviteMemberDto.builder()
                .firstname(firstName)
                .lastname(lastName)
                .email(email)
                .allowedAllUrls(false)
                .allowedUrls(new Long[]{1L})
                .roles(Set.of(MemberRole.ORGANIZATION_MEMBER))
                .build();
        String body = objectMapper.writeValueAsString(inviteDto);
        mockMvc.perform(post(API_USER + "/organizations/" + orgSlug + "/members")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }



}
