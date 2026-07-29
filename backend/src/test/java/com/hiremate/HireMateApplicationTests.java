package com.hiremate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class HireMateApplicationTests {

    @Test
    void contextLoads() {
        // Verify Spring ApplicationContext loads cleanly
    }
}
