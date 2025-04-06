package com.mpanov.diploma.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ImageServiceTest {

    @Autowired
    private ImageService imageService;

    private final SecureRandom RND = new SecureRandom();

    @Test
    @DisplayName("Should save org avatar to S3")
    public void shouldSaveOrgAvatarToS3() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("test.avif")) {
            assert is != null;
            Long organizationId = Math.abs(RND.nextLong());
            String result = imageService.saveOrganizationAvatar(is.readAllBytes(), organizationId);
            assertThat(result).startsWith("https://cdn.mpanov.com/dev/images/organization/" + organizationId + "/");
            System.err.println(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Should remove org avatar from S3")
    public void shouldRemoveOrgAvatarFromS3() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("test.avif")) {
            assert is != null;
            Long organizationId = Math.abs(RND.nextLong());
            String resultUrl = imageService.saveOrganizationAvatar(is.readAllBytes(), organizationId);
            assertThat(resultUrl).startsWith("https://cdn.mpanov.com/dev/images/organization/" + organizationId + "/");
            imageService.removeOrganizationAvatar(organizationId, resultUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Should save user avatar to S3")
    public void shouldSaveUserAvatarToS3() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("test.avif")) {
            assert is != null;
            Long userId = Math.abs(RND.nextLong());
            String result = imageService.saveUserProfilePicture(userId, is.readAllBytes());
            assertThat(result).startsWith("https://cdn.mpanov.com/dev/images/user/" + userId + "/");
            System.err.println(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Should remove user avatar from S3")
    public void shouldRemoveUserAvatarFromS3() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("test.avif")) {
            assert is != null;
            Long userId = Math.abs(RND.nextLong());
            String resultUrl = imageService.saveUserProfilePicture(userId, is.readAllBytes());
            assertThat(resultUrl).startsWith("https://cdn.mpanov.com/dev/images/user/" + userId + "/");
            imageService.removeUserProfilePicture(userId, resultUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
