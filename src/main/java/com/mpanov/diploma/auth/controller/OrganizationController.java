package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.auth.dto.organization.*;
import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.security.ActorContext;
import com.mpanov.diploma.auth.security.JwtPayloadService;
import com.mpanov.diploma.auth.service.OrganizationService;
import com.mpanov.diploma.auth.service.ServiceUserLogic;
import com.mpanov.diploma.data.MemberPermission;
import com.mpanov.diploma.data.dto.AbstractResponseDto;
import com.mpanov.diploma.data.dto.TokenResponseDto;
import com.mpanov.diploma.data.security.JwtUserSubject;
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

    private final ServiceUserLogic serviceUserLogic;

    private final JwtPayloadService jwtPayloadService;

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
                directionOpt.orElse("asc"),
                sortByOpt
                        .filter("members"::equals)
                        .map(sb -> new String[] {"membersCount", "name"})
                        .orElse(new String[] {"name"})
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

        Organization organization = organizationService.getOrganizationBySlugThrowable(slug);

        OrganizationDto rv = mapper.toOrganizationDto(organization);

        return new AbstractResponseDto<>(rv);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AbstractResponseDto<TokenResponseDto> createNewOrganization(
            @Valid @RequestBody CreateOrganizationDto createOrganizationDto
    ) {
        ServiceUser authenticatedUser = actorContext.getAuthenticatedUser();
        log.info("Requested POST /user/organizations for userId={}, dto={}", authenticatedUser.getId(), createOrganizationDto.toString());
        organizationService.createOrganizationByUser(authenticatedUser, createOrganizationDto);

        JwtUserSubject newSubject = serviceUserLogic.loginWithUserIdBySystem(authenticatedUser.getId());

        TokenResponseDto rv = jwtPayloadService.getTokensForUserSubject(newSubject);
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

    @DeleteMapping("/{slug}/avatar")
    public AbstractResponseDto<OrganizationDto> deleteOrganizationAvatar(
            @PathVariable String slug
    ) {
        log.info("Requested DELETE /user/organizations/{}/avatar", slug);
        actorContext.assertHasAccessToOrganization(slug, MemberPermission.MANAGE_ORGANIZATION);

        Organization organization = organizationService.removeOrganizationAvatar(slug);

        OrganizationDto rv = mapper.toOrganizationDto(organization);
        return new AbstractResponseDto<>(rv);
    }

    @DeleteMapping("/{slug}")
    public AbstractResponseDto<TokenResponseDto> deleteOrganization(
            @PathVariable String slug
    ) {
        Long userId = actorContext.getAuthenticatedUser().getId();
        log.info("Requested DELETE /user/organizations/{} by userId={}", slug, userId);
        actorContext.assertHasAccessToOrganization(slug, MemberPermission.FULL_ACCESS);

        organizationService.removeOrganization(slug);

        JwtUserSubject newSubject = serviceUserLogic.loginWithUserIdBySystem(userId);

        TokenResponseDto rv = jwtPayloadService.getTokensForUserSubject(newSubject);
        return new AbstractResponseDto<>(rv);
    }
}
