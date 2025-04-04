package com.mpanov.diploma.auth.controller;

import com.mpanov.diploma.data.dto.*;
import com.mpanov.diploma.data.exception.PlatformException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.mpanov.diploma.auth.config.SecurityConfig.API_PUBLIC;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PlatformControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should check service health")
    public void shouldCheckServiceHealth() throws Exception {
        this.mockMvc.perform(get(API_PUBLIC + "/platform/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payloadType").value(HealthResponseDto.class.getSimpleName()))
                .andExpect(jsonPath("$.payload.status").value("UP"));
    }

    @Test
    @DisplayName("Should check service error")
    public void shouldCheckServiceError() throws Exception {
        this.mockMvc.perform(get(API_PUBLIC + "/platform/error"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Demo platform error"))
                .andExpect(jsonPath("$.errors[0].errorType").value(ServiceErrorType.PLATFORM_ERROR.toString()))
                .andExpect(jsonPath("$.errors[0].errorClass").value(PlatformException.class.getSimpleName()));
    }

}
