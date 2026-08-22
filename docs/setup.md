# Setup Guide

This guide explains how to prepare the Spring profile configuration files before running the IAM service locally or with Docker.

## Configuration Files

Spring Boot loads configuration from `src/main/resources/`. The service uses:

- `application-local.yaml` for local Gradle runs
- `application-docker.yaml` for Docker Compose runs

The repository includes `application-local.txt` and `application-docker.txt` as reference templates. The TXT files are not loaded by Spring Boot.

## Run Commands

Run the local deployment first when developing on your machine:

```bash
# Create the local profile configuration from the reference template
cp src/main/resources/application-local.txt src/main/resources/application-local.yaml

# Edit application-local.yaml and set JWT_SECRET_KEY and any local credentials

# Start the service with the local profile
./gradlew bootRun
```

The local service runs at `http://localhost:4200` and requires MongoDB, RabbitMQ, and Eureka to be running at the configured addresses.

Run the Docker deployment after preparing the Docker profile:

```bash
# Create the Docker profile configuration from the reference template
cp src/main/resources/application-docker.txt src/main/resources/application-docker.yaml

# Edit application-docker.yaml and set JWT_SECRET_KEY and deployment credentials

# Create the shared network if it does not already exist
docker network inspect los-network >/dev/null 2>&1 || docker network create los-network

# Build and start the service and MongoDB
docker compose up --build
```

The Docker deployment exposes the service at `http://localhost:4200`. RabbitMQ and Eureka are expected to be available on `los-network`; they are not defined in this Compose file.

## Local Deployment Details

Before running the local commands:

- Set `MONGO_URI`, normally `mongodb://localhost:27017/user_db`.
- Set `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, and `RABBITMQ_PASS`.
- Set `EUREKA_HOST`, normally `localhost`.
- Set `JWT_SECRET_KEY` to a strong local-only secret.
- Make sure the JDK 25 toolchain is installed.

The default local profile expects MongoDB at `localhost:27017`, RabbitMQ at `localhost:5672`, and Eureka at `localhost:8761`.

## Docker Deployment Details

Before running the Docker commands, verify the container settings in `application-docker.yaml`:

- MongoDB: `iam-mongo-db` and database `user_db`
- RabbitMQ: `rabbitmq-container` on port `5672`
- Eureka: `registry-service` on port `8761`
- `JWT_SECRET_KEY`: use a deployment secret rather than a committed value

These hostnames must resolve on the shared Docker network used by the dependent services.

## Security Notes

- Replace the blank `JWT_SECRET_KEY` in each YAML file before starting the service.
- Use different secrets for local development and deployed environments.
- Do not commit real passwords, tokens, JWT secrets, or private environment configuration.
- The TXT files are safe references only when they contain no real credentials. Keep deployment secrets in your environment or secret manager.

## Useful Commands

```bash
# Run regular tests
./gradlew test

# Run end-to-end tests when dependent infrastructure is available
./gradlew e2eTest

# Stop Docker services
docker compose down
```
