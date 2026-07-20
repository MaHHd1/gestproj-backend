# gestproj-backend

![CI - Tests Backend](https://github.com/MaHHd1/gestproj-backend/actions/workflows/ci.yml/badge.svg)
![CD - Docker Build](https://github.com/MaHHd1/gestproj-backend/actions/workflows/cd.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)

Spring Boot backend for the GestProj collaborative project management application.

## 🧪 Linting Tools

This project uses the following linting tools to maintain code quality and consistency:

| Tool | Purpose | Command |
|------|---------|---------|
| **Checkstyle** | Enforces code style and formatting | `./mvnw checkstyle:check` |
| **PMD** | Finds code smells and anti-patterns | `./mvnw pmd:check` |
| **SpotBugs** | Detects potential bugs and security issues | `./mvnw spotbugs:check` |
| **Spotless** | Auto-formats code (uses Google Java Format) | `./mvnw spotless:apply` |

### Running All Linting Checks

    ./mvnw checkstyle:check pmd:check spotbugs:check

### Auto-Formatting Code

    ./mvnw spotless:apply

## Local development

Start PostgreSQL locally, then run:

    ./mvnw spring-boot:run

Default local database settings are:

    SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/gestproj
    SPRING_DATASOURCE_USERNAME=postgres
    SPRING_DATASOURCE_PASSWORD=123

These values can be overridden with environment variables.

## Docker Compose

Run the backend with PostgreSQL:

    docker compose up --build

The API is available at:

    http://localhost:8080

Swagger UI:

    http://localhost:8080/swagger-ui.html

## Email invitations

Invitation emails are disabled by default. To enable SMTP delivery, configure:

    MAIL_ENABLED=true
    MAIL_HOST=smtp.resend.com
    MAIL_PORT=587
    MAIL_USERNAME=resend
    MAIL_PASSWORD=<resend-api-key>
    MAIL_FROM=no-reply@your-domain.com
    MAIL_SMTP_AUTH=true
    MAIL_SMTP_STARTTLS_ENABLE=true
    APP_FRONTEND_URL=http://localhost:3000

The recommended provider for this project is Resend via SMTP.

## Tests

    ./mvnw test