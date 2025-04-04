package com.mpanov.diploma.auth.dto.organization.members;

import com.mpanov.diploma.auth.dto.common.PagedResponse;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationMembersListDto extends PagedResponse {

    private List<OrganizationMemberDto> entries;

}
