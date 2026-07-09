# gestproj-backend

Spring Boot backend for the GestProj collaborative project management application.

## Local development

Start PostgreSQL locally, then run:

```bash
./mvnw spring-boot:run
```

Default local database settings are:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/gestproj
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=123
```

These values can be overridden with environment variables.

## Docker Compose

Run the backend with PostgreSQL:

```bash
docker compose up --build
```

The API is available at:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## Email invitations

Invitation emails are disabled by default. To enable SMTP delivery, configure:

```text
MAIL_ENABLED=true
MAIL_HOST=smtp.resend.com
MAIL_PORT=587
MAIL_USERNAME=resend
MAIL_PASSWORD=<resend-api-key>
MAIL_FROM=no-reply@your-domain.com
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true
APP_FRONTEND_URL=http://localhost:3000
```

The recommended provider for this project is Resend via SMTP.

## Tests

```bash
./mvnw test
```

