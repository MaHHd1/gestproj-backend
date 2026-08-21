# gestproj-backend

![CI - Tests Backend](https://github.com/MaHHd1/gestproj-backend/actions/workflows/ci.yml/badge.svg)
![CD - Docker Build](https://github.com/MaHHd1/gestproj-backend/actions/workflows/cd.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)

Spring Boot REST API for the GestProj collaborative project-management application.

## Quality checks

| Tool | Purpose | Command |
|---|---|---|
| Checkstyle | Enforces code style and formatting | `./mvnw checkstyle:check` |
| PMD | Finds code smells and anti-patterns | `./mvnw pmd:check` |
| SpotBugs | Detects potential bugs and security issues | `./mvnw spotbugs:check` |
| Spotless | Formats Java code with Google Java Format | `./mvnw spotless:apply` |

Run all static checks with:

```bash
./mvnw checkstyle:check pmd:check spotbugs:check
```

## Local development

Start PostgreSQL locally, then run:

```bash
./mvnw spring-boot:run
```

Default local database settings:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/gestproj
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=123
```

## Docker Compose

Run the backend with PostgreSQL:

```bash
docker compose up --build
```

The API is available at `http://localhost:8080`, with Swagger UI at `http://localhost:8080/swagger-ui.html`.

## Email invitations

Invitation emails are disabled by default. Enable SMTP delivery with:

```env
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

Resend via SMTP is the recommended provider.

## Public access with Tailscale Funnel

When this host is on Tailscale, the included frontend container serves both the Angular
application and the API under one origin. Build and start it with:

```bash
docker compose up --build -d
```

Then, on the host that runs Docker, publish the frontend over HTTPS:

```bash
tailscale funnel 8081
```

Tailscale prints the resulting public `https://<machine>.<tailnet>.ts.net` URL. Set
`APP_FRONTEND_URL` to that exact URL before enabling email, so invitation and password-reset
links point to the public application. The frontend is bound to `127.0.0.1:8081`; the backend
continues to be available on port 8080 for local administration.

## Deployment monitoring and server integration

Projects can optionally be linked to a Gitea repository and a deployment target. The backend exposes repository activity and deployment history for the frontend.

- `GET /projects/{projectId}/commits` returns recent commits for a linked Gitea repository.
- `GET /projects/{projectId}/deployments` returns deployment history for active project members.
- `POST /projects/{projectId}/deployments` records a deployment. The caller must be the project owner or have the task-creation permission.
- Project updates accept `repoOwner`, `repoName`, `deploymentHost`, and `deploymentContainer`.

Deployment entries contain workflow metadata, commit details, deployment target, Docker health status, and diagnostics. A failed deployment creates an in-app notification for the project owner and is recorded in the project activity log.

The Gitea Actions backend workflow runs quality checks, rebuilds the Docker image, replaces the `backend` container on `infra_infra_net`, waits for `/actuator/health`, and records the result in PostgreSQL. The deployed frontend is allowed by CORS at `http://100.83.8.6:4200`; add any different production origin in `SecurityConfig` before deployment.

Configure Gitea access with:

```env
GITEA_BASE_URL=http://<gitea-host>:3000
GITEA_TOKEN=<optional-access-token>
```

## Tests

```bash
./mvnw test
```
Aa