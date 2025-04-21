package com.mpanov.diploma.auth.dto.organization.members;

import com.mpanov.diploma.data.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationMemberDto {

    private Long id;

    private Long organizationId;

    private String fullName;

    private String email;

    private Set<MemberRole> roles;

    private Set<Long> allowedUrls;

    private Boolean allowedAllUrls;

}
