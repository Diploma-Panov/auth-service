package com.mpanov.diploma.auth.dto.organization.members;

import com.mpanov.diploma.data.MemberRole;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberRolesDto {

    @NotNull
    @Positive
    private Long memberId;

    @NotNull
    @NotEmpty
    private Set<MemberRole> newRoles;

}
