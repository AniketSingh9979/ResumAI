package com.resumai.mailservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.mail.host=localhost",
    "spring.mail.port=25"
})
class MailServiceApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures that the Spring context loads successfully
        // and all configurations are valid
    }
} 