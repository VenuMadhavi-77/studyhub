package com.studyhub.platform.modules.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhub.platform.modules.auth.dto.LoginRequest;
import com.studyhub.platform.modules.auth.dto.RegisterRequest;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserRegistrationPersistenceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerAndLogin_ShouldPersistUserAndReturnValidJwt() throws Exception {
        String uniqueEmail = "persisted.student." + System.currentTimeMillis() + "@studyhub.edu";
        String password = "SecureStudentPass#2026";

        RegisterRequest registerRequest = new RegisterRequest(
                uniqueEmail,
                password,
                "Persistent Student",
                1L,
                4
        );

        // 1. Register User
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(uniqueEmail.toLowerCase()))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        // 2. Verify existence in DB
        assertTrue(userRepository.findByEmail(uniqueEmail.toLowerCase()).isPresent(),
                "Registered user must be present in UserRepository");

        // 3. Login with credentials
        LoginRequest loginRequest = new LoginRequest(uniqueEmail, password);
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.email").value(uniqueEmail.toLowerCase()));
    }
}
