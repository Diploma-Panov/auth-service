package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.auth.dto.organization.members.InviteMemberDto;
import com.mpanov.diploma.auth.dto.organization.members.OrganizationMembersListDto;
import com.mpanov.diploma.auth.dto.organization.members.UpdateMemberRolesDto;
import com.mpanov.diploma.auth.dto.organization.members.UpdateMemberUrlsDto;
import com.mpanov.diploma.auth.model.OrganizationMember;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.security.ActorContext;
import com.mpanov.diploma.auth.service.OrganizationMembersService;
import com.mpanov.diploma.data.MemberPermission;
import com.mpanov.diploma.data.dto.AbstractResponseDto;
import com.mpanov.diploma.data.dto.MessageResponseDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static com.mpanov.diploma.auth.config.SecurityConfig.API_USER;

@Slf4j
@RestController
@RequestMapping(API_USER + "/organizations/{slug}/members")
@AllArgsConstructor
public class OrganizationMembersController {

    private final OrganizationMembersService organizationMembersService;

    private final Mapper mapper;

    private final ActorContext actorContext;

    @GetMapping
    public AbstractResponseDto<OrganizationMembersListDto> getOrganizationMembers(
            @PathVariable("slug") String slug,
            @RequestParam(name = "p") Optional<Integer> pageOpt,
            @RequestParam(name = "q") Optional<Integer> quantityOpt,
            @RequestParam(name = "sb") Optional<String> sortByOpt,
            @RequestParam(name = "dir") Optional<String> directionOpt
    ) {
        log.info("Requested GET /user/organizations/{}/members", slug);

        actorContext.assertHasAccessToOrganization(slug, MemberPermission.BASIC_VIEW);

        String[] sortFields = sortByOpt
                .filter("name"::equals)
                .map(sb -> new String[]{"displayFirstname", "displayLastname", "memberUser.firstname", "memberUser.lastname"})
                .orElse(new String[]{"memberUser.email"});

        Pageable pageable = mapper.toPageable(
                pageOpt.orElse(0),
                quantityOpt.orElse(10),
                directionOpt.orElse("asc"),
                sortFields
        );

        List<OrganizationMember> members = organizationMembersService.getOrganizationMembersBySlug(slug, pageable);

        int total = organizationMembersService.countOrganizationMembersBySlug(slug);

        OrganizationMembersListDto rv = mapper.toOrganizationMembersListDto(members,
                total,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return new AbstractResponseDto<>(rv);
    }

    @PostMapping
    public AbstractResponseDto<MessageResponseDto> inviteNewOrganizationMember(
            @PathVariable String slug,
            @Valid @RequestBody InviteMemberDto dto
    ) {
        log.info("Requested POST /user/organizations/{}/members, with dto={}", slug, dto);

        actorContext.assertHasAccessToOrganization(slug, MemberPermission.INVITE_MEMBERS);

        organizationMembersService.inviteNewOrganizationMember(slug, dto);

        return new AbstractResponseDto<>(MessageResponseDto.success());
    }

    @PutMapping("/{memberId}/roles")
    public AbstractResponseDto<MessageResponseDto> updateOrganizationMemberRoles(
            @PathVariable String slug,
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateMemberRolesDto dto
    ) {
        log.info("Requested PUT /user/organizations/{}/members/roles, with dto={}", slug, dto);

        actorContext.assertHasAccessToOrganization(slug, MemberPermission.MANAGE_MEMBERS);

        ServiceUser actorUser = actorContext.getAuthenticatedUser();

        organizationMembersService.updateMemberRoles(slug, actorUser, dto, memberId);

        return new AbstractResponseDto<>(MessageResponseDto.success());
    }

    @PutMapping("/{memberId}/urls")
    public AbstractResponseDto<MessageResponseDto> updateOrganizationMemberUrls(
            @PathVariable String slug,
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateMemberUrlsDto dto
    ) {
        log.info("Requested PUT /user/organizations/{}/members/{}/urls, with dto={}", slug, memberId, dto);

        actorContext.assertHasAccessToOrganization(slug, MemberPermission.MANAGE_URLS);

        ServiceUser actorUser = actorContext.getAuthenticatedUser();

        organizationMembersService.updateMemberUrls(slug, memberId, actorUser, dto);

        return new AbstractResponseDto<>(MessageResponseDto.success());
    }

    @DeleteMapping("/{memberId}")
    public AbstractResponseDto<MessageResponseDto> deleteOrganizationMember(
            @PathVariable String slug,
            @PathVariable Long memberId
    ) {
        log.info("Requested DELETE /user/organizations/{}/members/{}", slug, memberId);

        actorContext.assertHasAccessToOrganization(slug, MemberPermission.MANAGE_MEMBERS);

        ServiceUser actorUser = actorContext.getAuthenticatedUser();

        organizationMembersService.deleteOrganizationMember(memberId, actorUser, slug);

        return new AbstractResponseDto<>(MessageResponseDto.success());
    }

}
