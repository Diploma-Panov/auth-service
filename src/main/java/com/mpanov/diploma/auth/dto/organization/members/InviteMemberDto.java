package com.mpanov.diploma.auth.dto.organization.members;

import com.mpanov.diploma.data.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteMemberDto {

    @NotBlank
    private String firstname;

    @NotBlank
    private String lastname;

    @Email
    @NotBlank
    private String email;

    @NotNull
    private Boolean allowedAllUrls;

    @NotNull
    private Long[] allowedUrls;

    @NotEmpty
    private Set<MemberRole> roles;

}
