package com.mpanov.diploma.auth.service;

import com.mpanov.diploma.utils.RandomUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ImageService {

    public String saveOrganizationAvatar(byte[] imageBytes, Long organizationId) {
        log.info("saveOrganizationAvatar: for organizationId={}", organizationId);
        String token = RandomUtils.generateRandomAlphabeticalString(10);
        return "images/organization/" + organizationId + "/" + token + ".png";
    }

    public void removeOrganizationAvatar(Long organizationId, String url) {
        log.info("removeOrganizationAvatar: for organizationId={} and url={}", organizationId, url);

    }

    public String saveUserProfilePicture(Long userId, byte[] imageBytes) {
        log.info("saveUserProfilePicture: for userId={}", userId);
        String token = RandomUtils.generateRandomAlphabeticalString(10);
        return "images/user/" + userId + "/" + token + ".png";
    }

    public void removeUserProfilePicture(Long userId, String url) {
        log.info("removeUserProfilePicture: for userId={} and url={}", userId, url);
    }

}
