package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.auth.dto.organization.members.OrganizationMembersListDto;
import com.mpanov.diploma.auth.dto.organization.members.UpdateMemberUrlsDto;
import com.mpanov.diploma.auth.model.OrganizationMember;
import com.mpanov.diploma.auth.service.OrganizationMembersService;
import com.mpanov.diploma.auth.service.ServiceUserLogic;
import com.mpanov.diploma.data.dto.AbstractResponseDto;
import com.mpanov.diploma.data.dto.TokenResponseDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.mpanov.diploma.auth.config.SecurityConfig.API_SYSTEM;

@Slf4j
@RestController
@RequestMapping(API_SYSTEM + "/members/")
@AllArgsConstructor
public class OrganizationMembersSystemController {

    private OrganizationMembersService organizationMembersService;

    private ServiceUserLogic serviceUserLogic;

    private Mapper mapper;

    @PutMapping("/{memberId}/urls")
    public AbstractResponseDto<TokenResponseDto> updateMemberUrlsBySystem(@PathVariable Long memberId, @Valid @RequestBody UpdateMemberUrlsDto dto) {
        log.info("Updating member urls for {} by system", memberId);
        Long userId = organizationMembersService.updateMemberUrlsBySystem(memberId, dto);
        TokenResponseDto rv = serviceUserLogic.loginAsUserBySystem(userId);
        return new AbstractResponseDto<>(rv);
    }

    @GetMapping("/of-user/{userId}")
    public AbstractResponseDto<OrganizationMembersListDto> getOrganizationMembersOfUser(@PathVariable Long userId) {
        log.info("Retrieving organization members of user {}", userId);
        List<OrganizationMember> members = organizationMembersService.getOrganizationMembersByUserId(userId);
        OrganizationMembersListDto rv = mapper.toOrganizationMembersListDto(members, members.size(), 0, members.size());
        return new AbstractResponseDto<>(rv);
    }

}
