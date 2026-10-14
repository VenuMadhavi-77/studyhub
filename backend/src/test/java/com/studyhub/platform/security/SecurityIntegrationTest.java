package com.studyhub.platform.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedRequest_ToProtectedEndpoint_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student@test.edu", roles = {"STUDENT"})
    void studentUser_AccessingAdminEndpoint_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@studyhub.edu", roles = {"ADMIN"})
    void adminUser_AccessingAdminEndpoint_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isOk());
    }

    @Test
    void publicTaxonomyEndpoint_ShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/taxonomy/departments"))
                .andExpect(status().isOk());
    }

    @Test
    void optionsPreflight_ToTaxonomyEndpoint_ShouldReturnOkWithCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/v1/taxonomy/departments")
                        .header("Origin", "https://studyhub-frontend-yxlr.onrender.com")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://studyhub-frontend-yxlr.onrender.com"));
    }

    @Test
    void getTaxonomyEndpoint_WithRenderOrigin_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/taxonomy/departments")
                        .header("Origin", "https://studyhub-frontend-yxlr.onrender.com"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://studyhub-frontend-yxlr.onrender.com"));
    }

    @Test
    void searchResourcesEndpoint_ShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/search/resources"))
                .andExpect(status().isOk());
    }
}
