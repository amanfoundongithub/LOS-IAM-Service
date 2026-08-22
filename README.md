# Identity and Access Management Service

Identity and access management microservice for LOS (Loan Origination System) users. It manages user registration, authentication, authorization, account lifecycle operations, tokens, password recovery, and audit events for the wider LOS platform.

## Technology

- Java 25 and Spring Boot 4.1
- MongoDB for user, token, and audit data
- RabbitMQ for asynchronous notification events
- JWT access and refresh tokens
- Netflix Eureka service discovery
- Spring Security, Actuator, and Springdoc OpenAPI

## Local Setup

See the [setup guide](docs/SETUP.md) for prerequisites, profile configuration, local deployment, and Docker deployment instructions. The TXT files in `src/main/resources/` are reference templates for creating the YAML files loaded by Spring Boot:

- `application-local.yaml` for `./gradlew bootRun`
- `application-docker.yaml` for Docker Compose

Do not commit real credentials or secret keys.

## API Documentation

With the service running, use the generated documentation:

- Swagger UI: `http://localhost:4200/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:4200/v3/api-docs`
- Health and application info: `http://localhost:4200/actuator/health` and `http://localhost:4200/actuator/info`

### Main endpoints

All API routes are under `/api/v1`.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/auth/register` | Register a user and initiate email activation |
| `POST` | `/auth/login` | Authenticate with email or username and password |
| `POST` | `/auth/refresh` | Refresh an access token |
| `POST` | `/auth/refresh/login` | Authenticate using a refresh token |
| `GET` | `/auth/verify-email` | Verify an email activation token |
| `POST` | `/auth/logout` | Log out and revoke the refresh token |
| `POST` | `/auth/password/reset` | Request a password reset email |
| `POST` | `/auth/password/change` | Change the authenticated user's password |
| `GET` | `/auth/allUserRoles` | List available user roles |
| `GET` | `/auth/allUserStatus` | List available user statuses |
| `GET` | `/users/me` | Return the authenticated user's profile |
| `GET` | `/admin/users` | Search users (admin access) |
| `POST` | `/admin/lock` | Lock a user account (admin access) |
| `POST` | `/admin/unlock` | Unlock a user account (admin access) |
| `GET` | `/admin/dashboard/summary` | Return the admin dashboard summary |

Protected endpoints require an access token in the `Authorization: Bearer <token>` header. Request and response schemas, validation rules, and status codes are available in Swagger UI.

## Project Structure

```text
src/main/java/com/loan_org/identity_and_access_management/
├── admin/          Admin dashboard, user search, and account lock operations
├── auth/           Registration, login, logout, refresh, email verification, and password reset
├── config/         Spring and MongoDB application configuration
├── exception/      Domain exceptions and the global API error handler
├── messaging/      RabbitMQ configuration and notification publishing
├── middleware/     Security filters, authentication, rate limiting, and request context
├── token/          Access, refresh, email activation, and password reset token handling
├── userAudit/      User account modification audit documents and services
├── userEntity/     User MongoDB documents, roles, statuses, and repositories
└── userManagement/ User profile and account management services/controllers

src/main/resources/  Application profiles, reference templates, logging configuration, and password-reset pages
src/test/java/       Unit, integration, and end-to-end tests
docs/                Project documentation
build.gradle         Dependencies and Gradle test/task configuration
docker-compose.yml   Local container orchestration for the service and MongoDB
Dockerfile           Multi-stage image build
```

## Testing

Run the regular test suite with:

```bash
./gradlew test
```

End-to-end tests are named `*E2ETest` and can be run separately when the required infrastructure is available:

```bash
./gradlew e2eTest
```

## Contributing

1. Create a focused branch from the current main branch.
2. Keep changes scoped to one feature or fix and follow the existing package and naming conventions.
3. Add or update tests for behavior changes, including authorization and validation cases where relevant.
4. Run `./gradlew test` before opening a pull request. Run `./gradlew e2eTest` for changes that affect infrastructure or service integrations.
5. Update the OpenAPI annotations or this README when public endpoints, configuration, or operational behavior changes.
6. Never commit credentials, JWT secrets, tokens, generated build output, or local environment files.
7. Open a pull request with a concise description, test results, configuration changes, and any deployment considerations.
