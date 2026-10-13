package com.studyhub.platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class StudyHubApplicationTests {

    @Test
    void contextLoads() {
        // Verifies Spring context, JPA entity mapping, and all beans load cleanly
    }
}
