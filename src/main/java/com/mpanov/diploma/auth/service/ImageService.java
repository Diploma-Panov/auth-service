package com.mpanov.diploma.auth.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ImageService {

    public String saveOrganizationAvatar(byte[] imageBytes, Long organizationId) {
        log.info("saveOrganizationAvatar: for organizationId={}", organizationId);
        return "images/organization/" + organizationId + "/avatar.png";
    }

}
