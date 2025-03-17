package com.mpanov.diploma.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserSignupDto {

    @NotBlank
    @Email
    @Length(max = 255)
    private String username;

    @Length(min = 8, max = 64)
    private String password;

    @NotBlank
    @Length(min = 1, max = 63)
    private String firstName;

    @Length(max = 63)
    private String lastName;

    @Length(max = 15)
    private String phone;

}
