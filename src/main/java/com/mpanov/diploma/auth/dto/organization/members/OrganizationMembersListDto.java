package com.mpanov.diploma.auth.dto.organization.members;

import com.mpanov.diploma.data.dto.PagedResponse;
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
