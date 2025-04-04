package com.mpanov.diploma.auth.dto.user;

import com.mpanov.diploma.data.OrganizationScope;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupDto {

    @NotBlank
    @Email
    @Length(max = 255)
    private String username;

    @Length(min = 8, max = 64)
    private String password;

    @NotBlank
    @Length(min = 1, max = 255)
    private String firstName;

    @Length(max = 255)
    private String lastName;

    @Length(max = 255)
    private String companyName;

    @Length(max = 512)
    private String profilePictureBase64;

    @NotNull
    private OrganizationScope registrationScope;

    private String siteUrl;

}
