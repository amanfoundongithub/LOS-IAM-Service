package com.loan_org.identity_and_access_management.e2e.register;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loan_org.identity_and_access_management.auth.dto.UserRegistrationDto;
import com.loan_org.identity_and_access_management.user.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.hasKey;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class RegistrationFlowBadRequestE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.5");

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.11-management");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMQContainer::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQContainer::getAdminPassword);
    }

    @AfterEach
    void cleanUpDatabase() {
        userRepository.deleteAll();
    }


    @Test
    @DisplayName("Should return 400 Bad Request with explicit field error maps when constraint violations occur")
    void register_ShouldReturn400BadRequest_WhenPayloadViolatesValidationConstraints() throws Exception {

        // 1. GIVEN: Build a payload that deliberately violates multiple DTO annotation rules
        UserRegistrationDto malformedPayload = new UserRegistrationDto();
        malformedPayload.setEmail("invalid-email-format");   // Violates @Email
        malformedPayload.setUsername("abc");                  // Violates @Size(min = 4)
        malformedPayload.setPassword("");                     // Violates @NotBlank & @Size(min = 8)
        malformedPayload.setRole("   ");                      // Violates @NotBlank
        malformedPayload.setSigningLimit(-5000.00);           // Violates @PositiveOrZero

        // 2. WHEN: Send the malformed DTO directly through the real application filter pipeline
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(malformedPayload)))

                // 3. THEN: Assert that the network layer responds with an explicit 400 Bad Request
                .andExpect(status().isBadRequest())

                // Assert core ApiErrorResponse contract metadata alignment
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.path", is("/api/v1/auth/register")))
                .andExpect(jsonPath("$.timestamp", notNullValue()))

                // CRITICAL CONTRACT CHECK: Verify the validationErrors field IS present and populated
                .andExpect(jsonPath("$.validationErrors", hasKey("email")))
                .andExpect(jsonPath("$.validationErrors", hasKey("username")))
                .andExpect(jsonPath("$.validationErrors", hasKey("password")))
                .andExpect(jsonPath("$.validationErrors", hasKey("role")))
                .andExpect(jsonPath("$.validationErrors", hasKey("signingLimit")))

                // Verify that your custom DTO validation messages are accurately mapped to the keys
                .andExpect(jsonPath("$.validationErrors.email", is("Please provide valid email")))
                .andExpect(jsonPath("$.validationErrors.signingLimit", is("Signing limit cannot be negative")))
                .andExpect(jsonPath("$.validationErrors.username", is("Username must be between 4 and 20 characters")));
    }
}
