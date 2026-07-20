# gestproj-backend

![CI - Tests Backend](https://github.com/MaHHd1/gestproj-backend/actions/workflows/ci.yml/badge.svg)
![CD - Docker Build](https://github.com/MaHHd1/gestproj-backend/actions/workflows/cd.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)
![License](https://img.shields.io/badge/License-MIT-blue)

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

```bash
./mvnw checkstyle:check pmd:check spotbugs:check