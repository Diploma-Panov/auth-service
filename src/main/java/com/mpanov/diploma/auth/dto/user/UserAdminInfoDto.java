package com.mpanov.diploma.auth.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mpanov.diploma.auth.model.common.UserSystemRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminInfoDto {

    private Long id;

    private String firstname;

    private String lastname;

    private String companyName;

    private String email;

    private String profilePictureUrl;

    private UserSystemRole role;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime registrationDate;

}
