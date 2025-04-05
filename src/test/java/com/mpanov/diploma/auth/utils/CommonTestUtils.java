package com.mpanov.diploma.auth.utils;

import com.mpanov.diploma.auth.dto.user.UserSignupDto;
import com.mpanov.diploma.data.OrganizationScope;
import com.mpanov.diploma.data.security.PasswordService;
import com.mpanov.diploma.utils.RandomUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommonTestUtils {

    private final PasswordService passwordService;

    @Value("${test.user.email-template}")
    private String emailTemplate;


    public UserSignupDto generateSignupDto() {
        return UserSignupDto.builder()
                .username(generateRandomEmail())
                .password(passwordService.generateCompliantPassword())
                .firstName(RandomUtils.generateRandomAlphabeticalString(15))
                .lastName(RandomUtils.generateRandomAlphabeticalString(15))
                .companyName(RandomUtils.generateRandomAlphabeticalString(10))
                .registrationScope(OrganizationScope.SHORTENER_SCOPE)
                .siteUrl(generateRandomUrl())
                .build();
    }

    public String generateRandomEmail() {
        return emailTemplate.formatted(
                RandomUtils.generateRandomNumericString(10)
        );
    }

    public String generateRandomUrl() {
        return "https://" + RandomUtils.generateRandomAlphabeticalString(10) + ".com";
    }

}
