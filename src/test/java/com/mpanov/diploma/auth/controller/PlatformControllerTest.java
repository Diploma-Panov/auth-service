package com.mpanov.diploma.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpanov.diploma.auth.dto.*;
import com.mpanov.diploma.auth.exception.PlatformException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.mpanov.diploma.auth.config.SecurityConfig.API_PUBLIC;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PlatformControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should check service health")
    public void shouldCheckServiceHealth() throws Exception {
        AbstractResponseDto<HealthResponseDto> expectedObject = new AbstractResponseDto<>(HealthResponseDto.up());
        String expectedJson = objectMapper.writeValueAsString(expectedObject);
        this.mockMvc.perform(get(API_PUBLIC + "/platform/health"))
                .andExpect(status().isOk())
                .andExpect(
                        content().string(expectedJson)
                );

    }

    @Test
    @DisplayName("Should check service error")
    public void shouldCheckServiceError() throws Exception {
        ErrorResponseElement expectedError = new ErrorResponseElement(
                "Demo platform error",
                ServiceErrorType.PLATFORM_ERROR.toString(),
                PlatformException.class.getSimpleName()
        );
        ErrorResponseDto expectedObject = new ErrorResponseDto(
                List.of(expectedError)
        );
        String expectedJson = objectMapper.writeValueAsString(expectedObject);
        this.mockMvc.perform(get(API_PUBLIC + "/platform/error"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedJson));
    }

}
