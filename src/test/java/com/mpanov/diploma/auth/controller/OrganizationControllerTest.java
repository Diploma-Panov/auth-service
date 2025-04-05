package com.mpanov.diploma.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpanov.diploma.auth.utils.UserTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserTestUtils userTestUtils;

    @Autowired
    private ObjectMapper objectMapper;


}
