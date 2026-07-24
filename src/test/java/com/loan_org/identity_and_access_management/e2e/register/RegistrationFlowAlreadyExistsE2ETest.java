package com.loan_org.identity_and_access_management.e2e.register;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loan_org.identity_and_access_management.auth.dto.UserRegistrationDto;
import com.loan_org.identity_and_access_management.domain.user.repository.UserRepository;
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
import static org.hamcrest.Matchers.not;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class RegistrationFlowAlreadyExistsE2ETest {

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
    @DisplayName("Should return 409 Conflict when attempting to register an email that already exists")
    void register_ShouldReturn409Conflict_WhenEmailAlreadyExists() throws Exception {

        // 1. GIVEN: Create a base user payload and register them successfully
        String conflictEmail = "clash@loan_org.com";

        UserRegistrationDto primaryUser = new UserRegistrationDto();
        primaryUser.setEmail(conflictEmail);
        primaryUser.setUsername("primaryUser");
        primaryUser.setPassword("securePassword123!");
        primaryUser.setSigningLimit(50000.0);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(primaryUser)))
                .andExpect(status().isCreated());

        // 2. GIVEN: Construct a completely different user profile but with the EXACT same email address
        UserRegistrationDto duplicateUser = new UserRegistrationDto();
        duplicateUser.setEmail(conflictEmail); // Triggers the exception
        duplicateUser.setUsername("completelyNewUser");
        duplicateUser.setPassword("differentPassword999!");
        duplicateUser.setSigningLimit(10000.0);

        // 3. WHEN & THEN: Execute the second registration and assert the global advice handling behavior
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isConflict()) // Verifies response code is exactly 409

                // Assert the ApiErrorResponse record JSON mapping contract
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.path", is("/api/v1/auth/register")))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.message", notNullValue()))

                // CRITICAL CONTRACT VERIFICATION: Assert that 'validationErrors' is completely stripped out of the response body due to @JsonInclude
                .andExpect(jsonPath("$", not(hasKey("validationErrors"))));
    }

    @Test
    @DisplayName("Should return 409 Conflict when attempting to register a username that already exists")
    void register_ShouldReturn409Conflict_WhenUsernameAlreadyExists() throws Exception {

        String conflictUsername = "clash_username";

        // 1. GIVEN: Register a base user profile
        UserRegistrationDto primaryUser = new UserRegistrationDto();
        primaryUser.setEmail("unique.first@loan_org.com");
        primaryUser.setUsername(conflictUsername);
        primaryUser.setPassword("securePassword123!");
        primaryUser.setSigningLimit(50000.0);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(primaryUser)))
                .andExpect(status().isCreated());

        // 2. GIVEN: Create a second user with a unique email, but a duplicate username
        UserRegistrationDto duplicateUser = new UserRegistrationDto();
        duplicateUser.setEmail("unique.second@loan_org.com"); // Email is clean
        duplicateUser.setUsername(conflictUsername);        // Triggers username conflict path
        duplicateUser.setPassword("differentPassword999!");
        duplicateUser.setSigningLimit(10000.0);

        // 3. WHEN & THEN: Execute and verify that the global exception advisor acts uniformly
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isConflict())

                // Assert consistent API Contract fields
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.path", is("/api/v1/auth/register")))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.message", notNullValue()))

                // Assert that Jackson strips the validation map since this is an AccountAlreadyExistsException
                .andExpect(jsonPath("$", not(hasKey("validationErrors"))));
    }

}
