package com.mpanov.diploma.auth.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfilePictureDto {

    @NotBlank
    private String newProfilePictureBase64;

}
