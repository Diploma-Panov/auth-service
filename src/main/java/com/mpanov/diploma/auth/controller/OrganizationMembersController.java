package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.auth.dto.common.AbstractResponseDto;
import com.mpanov.diploma.auth.dto.organization.members.OrganizationMembersListDto;
import com.mpanov.diploma.auth.model.OrganizationMember;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.security.common.ActorContext;
import com.mpanov.diploma.auth.service.OrganizationMembersService;
import com.mpanov.diploma.data.MemberPermission;
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
        actorContext.assertHasAccessToOrganization(slug, MemberPermission.BASIC_VIEW);

        Pageable pageable = mapper.toPageable(
                pageOpt.orElse(0),
                quantityOpt.orElse(10),
                directionOpt.orElse("asc"),
                sortByOpt
                        .map(sb -> {
                            if (sb.equals("name")) {
                                return new String[] {
                                        "memberUser.firstname",
                                        "memberUser.lastname",
                                };
                            }
                            return new String[] {
                                    "memberUser.email"
                            };
                        })
                        .orElse(new String[] { "memberUser.email" })
        );

        ServiceUser authenticatedUser = actorContext.getAuthenticatedUser();

        log.info("Requested GET /user/organizations for userId={}", authenticatedUser.getId());

        List<OrganizationMember> members = organizationMembersService.getOrganizationMembersBySlug(slug, pageable);

        int total = organizationMembersService.countOrganizationMembersBySlug(slug);

        OrganizationMembersListDto rv = mapper.toOrganizationMembersListDto(members,
                total,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return new AbstractResponseDto<>(rv);
    }

}
