package com.mpanov.diploma.auth.dto.organization;

import com.mpanov.diploma.data.OrganizationScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationDto {

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(
            regexp = "^(?!-)(?!.*--)[a-z0-9-]+(?<!-)$",
            message = "Value can only contain lowercase letters, digits, and single hyphens (-). It cannot start or end with a hyphen or contain consecutive hyphens."
    )
    private String slug;

    @NotNull
    private OrganizationScope scope;

    private String url;

    private String description;

    private String avatarBase64;

}
