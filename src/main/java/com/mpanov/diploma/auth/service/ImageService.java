package com.mpanov.diploma.auth.service;

import com.mpanov.diploma.utils.RandomUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3Client s3Client;

    @Value("${platform.env}")
    private String platformEnv;

    @Value("${cdn.base-url}")
    private String cdnBaseUrl;

    @Value("${s3.image-bucket}")
    private String imageBucket;

    @Value("${platform.is-test}")
    private boolean isTest;

    public String saveOrganizationAvatar(byte[] imageBytes, Long organizationId) {
        log.info("saveOrganizationAvatar: for organizationId={}", organizationId);
        if (isTest) {
            log.info("saveOrganizationAvatar: skipping...");
            return RandomUtils.generateRandomAlphabeticalString(20);
        }
        String token = RandomUtils.generateRandomAlphabeticalString(10);
        String key = platformEnv + "/images/organization/" + organizationId + "/" + token + ".png";

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(imageBucket)
                    .key(key)
                    .contentType("image/png")
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));
        } catch (S3Exception e) {
            log.error("Failed to upload organization avatar for organizationId={}", organizationId, e);
            throw new RuntimeException("Failed to upload image to S3", e);
        }

        return cdnBaseUrl + key;
    }

    public void removeOrganizationAvatar(Long organizationId, String url) {
        log.info("removeOrganizationAvatar: for organizationId={} and url={}", organizationId, url);
        if (isTest) {
            log.info("removeOrganizationAvatar: skipping...");
            return;
        }
        String key = extractKeyFromUrl(url);
        if (key != null) {
            try {
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(imageBucket)
                        .key(key)
                        .build();
                s3Client.deleteObject(deleteObjectRequest);
            } catch (S3Exception e) {
                log.error("Failed to delete organization avatar for organizationId={}", organizationId, e);
                throw new RuntimeException("Failed to delete image from S3", e);
            }
        } else {
            log.warn("Could not extract key from url: {}", url);
            throw new RuntimeException("Could not extract key from url: " + url);
        }
    }

    public String saveUserProfilePicture(Long userId, byte[] imageBytes) {
        log.info("saveUserProfilePicture: for userId={}", userId);
        if (isTest) {
            log.info("saveUserProfilePicture: skipping...");
            return RandomUtils.generateRandomAlphabeticalString(20);
        }

        String token = RandomUtils.generateRandomAlphabeticalString(10);
        String key = platformEnv + "/images/user/" + userId + "/" + token + ".png";

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(imageBucket)
                    .key(key)
                    .contentType("image/png")
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));
        } catch (S3Exception e) {
            log.error("Failed to upload user profile picture for userId={}", userId, e);
            throw new RuntimeException("Failed to upload image to S3", e);
        }

        return cdnBaseUrl + key;
    }

    public void removeUserProfilePicture(Long userId, String url) {
        log.info("removeUserProfilePicture: for userId={} and url={}", userId, url);
        if (isTest) {
            log.info("removeUserProfilePicture: skipping...");
            return;
        }
        String key = extractKeyFromUrl(url);
        if (key != null) {
            try {
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(imageBucket)
                        .key(key)
                        .build();
                s3Client.deleteObject(deleteObjectRequest);
            } catch (S3Exception e) {
                log.error("Failed to delete user profile picture for userId={}", userId, e);
                throw new RuntimeException("Failed to delete image to S3", e);
            }
        } else {
            log.warn("Could not extract key from url: {}", url);
            throw new RuntimeException("Failed to delete user profile picture for userId=" + userId);
        }
    }

    private String extractKeyFromUrl(String url) {
        String prefix = cdnBaseUrl;
        if (url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }
        return null;
    }
}
