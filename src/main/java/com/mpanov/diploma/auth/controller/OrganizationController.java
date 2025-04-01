package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.auth.dto.common.AbstractResponseDto;
import com.mpanov.diploma.auth.dto.organization.*;
import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.security.common.ActorContext;
import com.mpanov.diploma.auth.service.OrganizationService;
import com.mpanov.diploma.data.MemberPermission;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static com.mpanov.diploma.auth.config.SecurityConfig.API_USER;

@Slf4j
@RestController
@RequestMapping(API_USER + "/organizations")
@AllArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    private final ActorContext actorContext;

    private final Mapper mapper;

    @GetMapping
    public AbstractResponseDto<OrganizationsListDto> getUserOrganizations(
            @RequestParam(name = "p") Optional<Integer> pageOpt,
            @RequestParam(name = "q") Optional<Integer> quantityOpt,
            @RequestParam(name = "sb") Optional<String> sortByOpt,
            @RequestParam(name = "dir") Optional<String> directionOpt
    ) {
        Pageable pageable = mapper.toPageable(
                pageOpt.orElse(0),
                quantityOpt.orElse(10),
                sortByOpt
                        .filter(sb -> sb.equals("id") || sb.equals("name"))
                        .orElse("id"),
                directionOpt.orElse("asc")
        );

        ServiceUser authenticatedUser = actorContext.getAuthenticatedUser();

        log.info("Requested GET /user/organizations for userId={}", authenticatedUser.getId());

        List<Organization> organizations = organizationService.getUserOrganizations(
                authenticatedUser, pageable
        );
        int total = organizationService.countUserOrganizations(authenticatedUser);

        OrganizationsListDto rv = mapper.toOrganizationsListDto(
                organizations,
                total,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return new AbstractResponseDto<>(rv);
    }

    @GetMapping("/{slug}")
    public AbstractResponseDto<OrganizationDto> getUserOrganizationBySlug(
            @PathVariable String slug
    ) {
        log.info("Requested GET /user/organizations/{}", slug);
        actorContext.assertHasAccessToOrganization(slug, MemberPermission.BASIC_VIEW);

        Organization organization = organizationService.getOrganizationBySlug(slug);

        OrganizationDto rv = mapper.toOrganizationDto(organization);

        return new AbstractResponseDto<>(rv);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AbstractResponseDto<OrganizationDto> createNewOrganization(
            @Valid @RequestBody CreateOrganizationDto createOrganizationDto
    ) {
        ServiceUser authenticatedUser = actorContext.getAuthenticatedUser();
        log.info("Requested POST /user/organizations for userId={}, dto={}", authenticatedUser.getId(), createOrganizationDto.toString());
        Organization newOrganization = organizationService.createOrganizationByUser(authenticatedUser, createOrganizationDto);
        OrganizationDto rv = mapper.toOrganizationDto(newOrganization);
        return new AbstractResponseDto<>(rv);
    }

    @PatchMapping("/{slug}")
    public AbstractResponseDto<OrganizationDto> updateOrganizationInfo(
            @Valid @RequestBody UpdateOrganizationInfoDto dto,
            @PathVariable String slug
    ) {
        log.info("Requested PATCH /user/organizations/{}, with dto={}", slug, dto.toString());
        actorContext.assertHasAccessToOrganization(slug, MemberPermission.MANAGE_ORGANIZATION);
        Organization updatedOrganization = organizationService.patchOrganizationWithPartialData(slug, dto);
        OrganizationDto rv = mapper.toOrganizationDto(updatedOrganization);
        return new AbstractResponseDto<>(rv);
    }

    @PutMapping("/{slug}/avatar")
    public AbstractResponseDto<OrganizationDto> updateOrganizationAvatar(
            @PathVariable String slug,
            @Valid @RequestBody UpdateOrganizationAvatarDto dto
            ) {
        log.info("Requested PUT /user/organizations/{}/avatar", slug);
        actorContext.assertHasAccessToOrganization(slug, MemberPermission.MANAGE_ORGANIZATION);

        Organization updatedOrganization = organizationService.updateOrganizationAvatar(slug, dto.getNewAvatarBase64());

        OrganizationDto rv = mapper.toOrganizationDto(updatedOrganization);
        return new AbstractResponseDto<>(rv);
    }

}
