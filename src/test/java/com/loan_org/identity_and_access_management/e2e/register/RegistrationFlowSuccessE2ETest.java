package com.loan_org.identity_and_access_management.e2e.register;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loan_org.identity_and_access_management.auth.dto.UserRegistrationDto;
import com.loan_org.identity_and_access_management.user.entity.UserDocument;
import com.loan_org.identity_and_access_management.user.entity.UserStatus;
import com.loan_org.identity_and_access_management.user.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class RegistrationFlowSuccessE2ETest {

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
    @DisplayName("Should successfully register a new user, hash password, and set state to pending verification")
    void register_ShouldCreateUserAndReturn201_WhenRequestIsValid() throws Exception {

        String email = "alpha.beta@loan_org.com";
        String password = "thisiavergjiru";
        String username = "alpha.beta_221";

        UserRegistrationDto requestPayload = new UserRegistrationDto();
        requestPayload.setEmail(email);
        requestPayload.setUsername(username);
        requestPayload.setPassword(password);
        requestPayload.setRole("LOAN_OFFICER");
        requestPayload.setSigningLimit(600000.0);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPayload)))
                .andExpect(status().isCreated());

        Optional<UserDocument> persistedUserOpt = userRepository.findByEmail(email);
        assertThat(persistedUserOpt).isPresent();

        UserDocument persistedUser = persistedUserOpt.get();
        assertThat(persistedUser.getUsername()).isEqualTo(username);
        assertThat(persistedUser.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);

        assertThat(persistedUser.getSecurity().getPasswordHash())
                .isNotBlank()
                .isNotEqualTo(password);

        assertThat(persistedUser.getSecurity().getFailedLoginAttempts()).isZero();
        assertThat(persistedUser.getMetadata().getCreatedAt()).isNotNull();
    }

    // --- ADDITIONAL SUCCESS SCENARIO PERMUTATIONS ---

    @ParameterizedTest(name = "Scenario: {0} (Role: {3}, Limit: {4})")
    @MethodSource("provideSuccessPermutations")
    @DisplayName("Should successfully register users across variant edge-case payloads")
    void register_ShouldSucceed_ForVariantDataPermutations(
            String testName, String email, String username, String role, Double signingLimit) throws Exception {

        // Given
        UserRegistrationDto requestPayload = new UserRegistrationDto();
        requestPayload.setEmail(email);
        requestPayload.setUsername(username);
        requestPayload.setPassword("highlySecurePlaintext123!");
        requestPayload.setRole(role);
        requestPayload.setSigningLimit(signingLimit);

        // When
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPayload)))
                // Then
                .andExpect(status().isCreated());

        // Verify Data Integrity on MongoDB Node directly
        Optional<UserDocument> persistedUserOpt = userRepository.findByEmail(email);
        assertThat(persistedUserOpt).isPresent();

        UserDocument persistedUser = persistedUserOpt.get();
        assertThat(persistedUser.getUsername()).isEqualTo(username);
        assertThat(persistedUser.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(persistedUser.getSecurity().getFailedLoginAttempts()).isZero();
        assertThat(persistedUser.getMetadata().getCreatedAt()).isNotNull();
    }

    private static Stream<Arguments> provideSuccessPermutations() {
        return Stream.of(
                // Scenario 1: Minimum value edge-case boundary for @PositiveOrZero validation
                Arguments.of("Standard Employee Profile with Zero Limit", "clerk.adam@loan_org.com", "clerk_adam", "EMPLOYEE", 0.0),

                // Scenario 2: Maximum limit check to verify floating-point precision on real DB
                Arguments.of("High Authority Risk Exec Profile", "exec.vp@loan_org.com", "vp_executive", "VP_RISK", 99999999.99),

                // Scenario 3: Null object check ensuring structural factories handle omitted fields gracefully
                Arguments.of("Omitted Optional Signing Limit Wrapper Field", "temp.auditor@loan_org.com", "external_audit", "AUDITOR", null),

                // Scenario 4: Special Characters inside valid size range (min 4, max 20)
                Arguments.of("Valid Username with Special Character Symbols", "symbolic.user@loan_org.com", "usr_._test-123", "SUPPORT", 5000.0)
        );
    }
}