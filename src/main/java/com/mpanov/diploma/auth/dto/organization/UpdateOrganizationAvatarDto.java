package com.mpanov.diploma.auth.dto.organization;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrganizationAvatarDto {

    @NotBlank
    private String newAvatarBase64;

}
