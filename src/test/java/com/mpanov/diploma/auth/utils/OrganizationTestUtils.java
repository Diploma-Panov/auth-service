package com.mpanov.diploma.auth.utils;

import com.mpanov.diploma.auth.dto.organization.CreateOrganizationDto;
import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.service.OrganizationService;
import com.mpanov.diploma.data.OrganizationScope;
import com.mpanov.diploma.utils.RandomUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationTestUtils {
   
    private final OrganizationService organizationService;

    private final CommonTestUtils commonTestUtils;

    public Organization createTestShortenerOrganizationForUser(ServiceUser user) {
        CreateOrganizationDto dto = CreateOrganizationDto.builder()
                .name(RandomUtils.generateRandomAlphabeticalString(20))
                .slug(this.generateRandomSlug())
                .scope(OrganizationScope.SHORTENER_SCOPE)
                .url(commonTestUtils.generateRandomUrl())
                .description(RandomUtils.generateRandomAlphabeticalString(30))
                .avatarBase64(RandomUtils.generateRandomAlphabeticalString(100))
                .build();
        return organizationService.createOrganizationByUser(user, dto);
    }

    public String generateRandomSlug() {
        return RandomUtils.generateRandomAlphabeticalString(5).toLowerCase() + "-" +
                RandomUtils.generateRandomAlphabeticalString(5).toLowerCase() + "-" +
                RandomUtils.generateRandomAlphabeticalString(5).toLowerCase();
    }


    
}
