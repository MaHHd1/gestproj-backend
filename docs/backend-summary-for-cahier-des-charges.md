# Backend Project Summary for Cahier des Charges

## 1. Project Overview

This backend is a Spring Boot REST API for a collaborative task management application. The application allows users to register, authenticate with JWT, create projects, invite other users, manage project members and permissions, create and manage tasks, receive notifications, and consult project activity logs.

The project is currently focused on backend development. Containerization and deployment exist as a later target, but the current backend is developed and tested locally with PostgreSQL.

## 2. Technical Stack

- Language: Java 21
- Framework: Spring Boot 4.1.0
- API type: REST API
- Build tool: Maven
- Security: Spring Security with stateless JWT authentication
- Password hashing: BCrypt
- Database: PostgreSQL 15
- Persistence: Spring Data JPA / Hibernate
- Database migrations: Flyway
- Validation: Jakarta Bean Validation through Spring Boot validation starter
- Testing: JUnit 5, Mockito, Spring Boot Test
- Local profile: `local`

Main Maven dependencies:

- `spring-boot-starter-webmvc`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `spring-boot-starter-actuator`
- `springdoc-openapi-starter-webmvc-ui` (v2.8.9)
- `postgresql`
- `flyway-core`
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson`
- `spring-boot-starter-test`
- `spring-boot-starter-security-test`

## 3. Configuration

Main configuration file:

- `src/main/resources/application.properties`

Important values:

- Application name: `backend`
- Active profile: `local`
- JWT secret configured through `app.jwt.secret`
- JWT expiration: `86400000` ms, equal to 24 hours
- Actuator endpoints exposed: `health`, `info`
- Swagger UI enabled at `/swagger-ui.html`
- OpenAPI docs available at `/v3/api-docs`

Local configuration file:

- `src/main/resources/application-local.properties`

Important values:

- Database URL: `jdbc:postgresql://localhost:5432/gestproj`
- Database username: `postgres`
- Hibernate mode: `validate`
- Flyway enabled
- Flyway migration folder: `classpath:db/migration`
- Open Session in View disabled with `spring.jpa.open-in-view=false`

## 4. Architectural Style

The backend uses a feature-oriented package structure. Each business feature has its own package containing controller, DTO, entity, repository, and service classes.

Main package:

```text
com.gestproj.backend
```

Feature packages:

```text
activitylog
auth
common
config
member
notification
project
projectinvitation
task
user
```

Typical feature organization:

```text
feature/
  controller/
  dto/
  entity/
  repository/
  service/
```

Reason for this organization:

- It keeps business logic grouped by feature.
- It makes the project easier to scale than a purely technical structure such as all controllers in one package and all services in another.
- It makes ownership and navigation easier: everything related to tasks is under `task`, everything related to invitations is under `projectinvitation`, etc.

## 5. Main Domain Entities

### User

Represents a registered platform user.

Table: `users`

Fields:

- `id`
- `email`, unique
- `username`, unique
- `password_hash`
- `name`
- `profile_image_url`

Responsibilities:

- Can register and login.
- Can own projects.
- Can be a member of many projects.
- Can be assigned to tasks.
- Can perform actions recorded in activity logs.

### Project

Represents a collaborative workspace.

Table: `projects`

Fields:

- `id`
- `name`
- `description`
- `owner_id`

Responsibilities:

- Belongs to one owner.
- Contains tasks.
- Contains members.
- Contains invitations, notifications, and activity logs.

### ProjectMember

Represents the relation between a user and a project, with project-specific role and permissions.

Table: `project_members`

Fields:

- `id`
- `project_id`
- `user_id`
- `role`: `OWNER`, `MEMBER`
- `status`: `INVITED`, `ACTIVE`, `SUSPENDED`
- `role_title`
- `role_description`
- `can_view_project`
- `can_create_task`
- `can_edit_task`
- `can_delete_task`
- `can_invite_member`
- `can_manage_members`

Important constraint:

- Unique pair: `(project_id, user_id)`

Responsibilities:

- Defines what a user can do inside a specific project.
- Supports agile/team role descriptions such as Developer, Scrum Master, Product Owner, Tester, etc.

### Task

Represents a unit of work inside a project.

Table: `tasks`

Fields:

- `id`
- `project_id`
- `title`
- `description`
- `status`: `A_FAIRE`, `EN_COURS`, `TERMINE`
- `priority`: `BASSE`, `MOYENNE`, `HAUTE`
- `due_date`
- `is_late`
- `assigned_to`
- `created_at`
- `updated_at`

Design choice:

- The task status and late state are separated.
- `status` describes progress.
- `is_late` describes deadline compliance.
- This avoids mixing two independent concepts. A task can be `EN_COURS` and late at the same time.

### ProjectInvitation

Represents an invitation to join a project.

Table: `project_invitations`

Fields:

- `id`
- `project_id`
- `invited_by`
- `invited_email`
- `token`, unique
- `status`: `PENDING`, `ACCEPTED`, `REJECTED`, `EXPIRED`
- `expires_at`
- `created_at`
- proposed member role and permission fields

Responsibilities:

- Allows project owners or authorized members to invite users.
- Supports invitation by email and invitation token.
- Defines the future role and permissions of the invited user.

### Notification

Represents an in-app notification for a user.

Table: `notifications`

Fields:

- `id`
- `user_id`
- `type`
- `title`
- `message`
- `read`
- `created_at`
- optional links to project, invitation, or member

Notification types:

- `INVITATION_SENT`
- `INVITATION_ACCEPTED`
- `INVITATION_REJECTED`
- `MEMBER_UPDATED`
- `MEMBER_STATUS_CHANGED`

### ActivityLog

Represents a chronological trace of important project actions.

Table: `activity_logs`

Fields:

- `id`
- `project_id`
- `user_id`
- `action`
- `created_at`

Examples of logged actions:

- Project created
- Task created
- Task updated
- Task deleted
- Invitation created
- Invitation accepted
- Member updated

## 6. Database Relations

Main relations:

```text
users 1 ---- N projects
users 1 ---- N project_members
projects 1 ---- N project_members
projects 1 ---- N tasks
users 1 ---- N tasks through assigned_to, optional
projects 1 ---- N project_invitations
projects 1 ---- N notifications
users 1 ---- N notifications
projects 1 ---- N activity_logs
users 1 ---- N activity_logs
```

The database schema is managed by Flyway:

```text
src/main/resources/db/migration/V1__initial_schema.sql
```

## 7. Security Model

Security is stateless and JWT-based.

Authentication flow:

1. User registers with email, username, password, and name.
2. Password is hashed with BCrypt.
3. On login, the backend returns a JWT.
4. Protected endpoints require `Authorization: Bearer <token>`.
5. A custom JWT filter validates the token and sets the authenticated user in the Spring Security context.

Public endpoints:

```text
POST /auth/register
POST /auth/login
GET  /actuator/health
GET  /actuator/info
GET  /swagger-ui/**
GET  /v3/api-docs/**
```

Protected endpoints:

```text
All endpoints except those listed above
```

Security configuration:

- CSRF disabled because the API is stateless.
- Session creation policy: `STATELESS`.
- Form login disabled.
- HTTP Basic disabled.
- JWT filter registered before `UsernamePasswordAuthenticationFilter`.

## 8. REST API Endpoints

### Health & Info Endpoints

```text
GET /actuator/health
GET /actuator/info
```

Behavior:

- `/actuator/health` returns application health status (UP/DOWN) with component details (database, disk space, etc.)
- `/actuator/info` returns application metadata
- Both endpoints are publicly accessible for monitoring

### API Documentation

```text
GET /swagger-ui.html
GET /v3/api-docs
```

Behavior:

- `/swagger-ui.html` provides an interactive Swagger UI for testing all REST endpoints
- `/v3/api-docs` returns the OpenAPI specification in JSON format
- Both endpoints are publicly accessible for API consumers and frontend integration

### Authentication

```text
POST /auth/register
POST /auth/login
GET  /auth/me
```

### Users

```text
GET /users/{id}
GET /users/search?query={email|username|name}
```

Note:

- Direct user creation through `POST /users` was removed.
- User creation must go through `/auth/register`.
- User search allows finding users by email, username, or name for project invitations.
- Returns list of matching users with id, email, username, name, and profile image URL.

### Projects

```text
POST   /projects
GET    /projects
GET    /projects/{id}
GET    /projects/{id}/statistics
PUT    /projects/{id}
DELETE /projects/{id}
```

Behavior:

- Creating a project makes the authenticated user the owner.
- A project owner is also inserted into `project_members` with role `OWNER`.
- `GET /projects` returns all projects where the authenticated user is a member.
- `GET /projects/{id}/statistics` returns project dashboard statistics:
  - `totalTasks`: Total number of tasks in the project
  - `completedTasks`: Number of completed tasks (status = TERMINE)
  - `inProgressTasks`: Number of in-progress tasks (status = EN_COURS)
  - `notStartedTasks`: Number of not started tasks (status = A_FAIRE)
  - `lateTasks`: Number of tasks marked as late
  - `completionPercentage`: Percentage of completed tasks (0-100)

### Tasks

```text
POST   /projects/{projectId}/tasks
GET    /projects/{projectId}/tasks
GET    /tasks/{taskId}
PUT    /tasks/{taskId}
DELETE /tasks/{taskId}
```

Task listing supports pagination and filters:

```text
GET /projects/{projectId}/tasks?page=0&size=10
GET /projects/{projectId}/tasks?status=A_FAIRE
GET /projects/{projectId}/tasks?priority=HAUTE
GET /projects/{projectId}/tasks?assignedToMe=true
GET /projects/{projectId}/tasks?overdue=true
```

### Task Comments

```text
POST   /tasks/{taskId}/comments
GET    /tasks/{taskId}/comments
DELETE /tasks/{taskId}/comments/{commentId}
```

Behavior:

- Enables team discussion and collaboration on specific tasks.
- Only project members can view and create comments on tasks.
- Comments include: `id`, `taskId`, `userId`, `username`, `userEmail`, `content`, `createdAt`, `updatedAt`.
- Comments are returned in reverse chronological order (newest first).
- Only the comment author can delete their own comments.
- Deleted comments are removed from the database (no soft delete).
- Perfect for team communication, task clarifications, and decision documentation.

### Invitations

Project invitation management:

```text
POST /projects/{projectId}/invites
GET  /projects/{projectId}/invites
```

Invitation actions:

```text
GET  /invites/{token}
POST /invites/{token}/accept
POST /invites/{token}/reject
```

### Members

```text
GET /projects/{projectId}/members
PUT /projects/{projectId}/members/{memberId}
```

Behavior:

- Owner or authorized users can update member role, status, title, description, and permissions.

### Notifications

```text
GET   /notifications
GET   /notifications/unread
PATCH /notifications/{id}/read
PATCH /notifications/read-all
```

### Activity Logs

```text
GET /projects/{projectId}/activity-logs
```

## 9. Implemented Functionalities

Implemented backend features:

- User registration
- User login
- JWT authentication
- Current user endpoint
- **User search** by email, username, or name (for project member invitation)
- Project CRUD
- **Project statistics** dashboard showing task completion metrics
- Automatic owner membership when project is created
- Project member listing
- Project member update
- Role and permission management per project
- Task CRUD
- Task assignment to a project member
- Task status and priority
- Due date and late flag
- Paginated task listing
- Task filters by status, priority, late state, and assigned user
- **Task comments** with full CRUD, permission checks, and reverse-chronological ordering
- Project invitations by email/token
- Invitation accept/reject
- Notifications for invitation and member events
- Activity logs for important project actions
- Global exception handling for not found, conflict, unauthorized, and forbidden errors
- Flyway database migration
- Local PostgreSQL configuration
- OpenAPI/Swagger documentation with interactive UI
- Actuator health and info endpoints for monitoring
- Scheduled job to automatically update task lateness flag
- **API rate limiting** (100 requests per minute per user for stability and security)

## 10. Tier 1 Enhancements (Recently Added)

In Phase 2 of backend development, four high-impact features were added to improve user experience and provide production-ready capabilities:

### 1. User Search (GET /users/search)

**Purpose**: Enable users to find other platform users for project member invitations.

**Endpoint**: 
```
GET /users/search?query={searchTerm}
```

**Implementation Details**:
- Search implementation in `UserRepository` using custom JPQL query with `LOWER()` and `LIKE` operators
- Case-insensitive search across three fields: email, username, and name
- Returns list of matching users with basic profile information
- Empty result set for null or empty query (safe default)

**Example Requests**:
```
GET /users/search?query=john
GET /users/search?query=john@example.com
GET /users/search?query=john.doe
```

**Response Example**:
```json
[
  {
    "id": "u-001",
    "email": "john.doe@example.com",
    "username": "johndoe",
    "name": "John Doe",
    "profileImageUrl": null
  },
  {
    "id": "u-002",
    "email": "jane.johnson@example.com",
    "username": "janeJ",
    "name": "Jane Johnson",
    "profileImageUrl": null
  }
]
```

**Use Case**: 
- Project owner or authorized member needs to invite a specific user
- User searches by email, username, or name
- System returns matching users
- Inviter selects desired user and sends invitation

### 2. Project Statistics Dashboard (GET /projects/{id}/statistics)

**Purpose**: Provide real-time project metrics for dashboard and progress visualization.

**Endpoint**:
```
GET /projects/{projectId}/statistics
```

**Implementation Details**:
- Statistics calculated in `ProjectService.getStatistics()` method
- Aggregates task data using Java streams for filtering and counting
- Metrics are computed fresh on each request (real-time)
- Completion percentage calculated as: (completedTasks / totalTasks) * 100, rounded to 2 decimals

**Response Example**:
```json
{
  "totalTasks": 25,
  "completedTasks": 10,
  "inProgressTasks": 8,
  "notStartedTasks": 7,
  "lateTasks": 2,
  "completionPercentage": 40.00
}
```

**Metrics Explained**:
- `totalTasks`: Sum of all tasks in the project (all statuses)
- `completedTasks`: Tasks with status = TERMINE
- `inProgressTasks`: Tasks with status = EN_COURS
- `notStartedTasks`: Tasks with status = A_FAIRE
- `lateTasks`: Tasks where is_late = true (regardless of status)
- `completionPercentage`: Percentage of completed tasks (0-100)

**Use Case**:
- Dashboard displays project health at a glance
- Project manager monitors task completion progress
- Stakeholders see project status in real-time
- No need to manually count tasks

### 3. Task Comments (Full CRUD with Permissions)

**Purpose**: Enable team members to collaborate and discuss task details within the application.

**Endpoints**:
```
POST   /tasks/{taskId}/comments
GET    /tasks/{taskId}/comments
DELETE /tasks/{taskId}/comments/{commentId}
```

**Database Changes**:
- New table `task_comments` (added via Flyway migration V2)
- Columns: id, task_id, user_id, content, created_at, updated_at
- Indexes on: task_id, user_id, created_at DESC
- Foreign key constraints with CASCADE delete on task, RESTRICT on user

**Implementation Details**:
- `TaskComment` entity with ManyToOne relationships to Task and User
- `TaskCommentService` enforces permission checks before every operation
- `TaskCommentRepository` provides custom query for fetching comments by task (ordered by created_at DESC)
- Comments returned in reverse chronological order (newest first)

**Permission Model**:
- Create: User must be a project member (checked via `ProjectMemberService.isMember()`)
- Read: User must be a project member
- Delete: Only the comment author can delete their own comment
- Database enforces RESTRICT on user_id foreign key (cannot delete a user with comments)

**Request/Response Example**:

Create comment:
```json
POST /tasks/{taskId}/comments
{
  "content": "This task needs clarification on the requirements."
}
```

Response:
```json
{
  "id": "tc-001",
  "taskId": "t-001",
  "userId": "u-001",
  "username": "johndoe",
  "userEmail": "john@example.com",
  "content": "This task needs clarification on the requirements.",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

Get comments on a task:
```json
GET /tasks/{taskId}/comments
[
  {
    "id": "tc-002",
    "taskId": "t-001",
    "userId": "u-002",
    "username": "janeJ",
    "userEmail": "jane@example.com",
    "content": "I've started investigating this.",
    "createdAt": "2024-01-15T11:00:00Z",
    "updatedAt": "2024-01-15T11:00:00Z"
  },
  {
    "id": "tc-001",
    "taskId": "t-001",
    "userId": "u-001",
    "username": "johndoe",
    "userEmail": "john@example.com",
    "content": "This task needs clarification on the requirements.",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
]
```

**Use Case**:
- Team member adds a question or clarification to a task
- Other members reply with insights or answers
- Full task discussion stays within the application (no need for external chat)
- Decisions and discussions are logged with timestamps and authors

### 4. API Rate Limiting (Security & Stability)

**Purpose**: Protect the API from abuse, prevent accidental flooding, and ensure fair resource usage.

**Configuration**:
- Hard-coded limit: **100 requests per minute per user**
- Applied to all endpoints globally
- Tracks per authenticated user (JWT token) or per IP address (fallback)

**Implementation Details**:
- `RateLimitInterceptor` extends `HandlerInterceptor`
- Registered in `WebMvcConfig` via `addInterceptors()`
- Uses `ConcurrentHashMap` with `RequestTracker` class managing time windows
- Time window resets every minute (tracks requests per calendar minute)
- JWT token takes precedence over IP address for identifying users

**Behavior**:
- Requests are tracked and counted per minute
- If count exceeds 100 in a minute window, subsequent requests get `429 TOO_MANY_REQUESTS`
- Response includes `Retry-After` header
- Time window automatically resets each minute

**Example Error Response**:
```
HTTP/1.1 429 Too Many Requests
Retry-After: 30

{
  "error": "Too many requests. Maximum 100 requests per minute allowed.",
  "retryAfter": 30
}
```

**Use Cases**:
- Prevents accidental DDoS from frontend bug (e.g., infinite polling)
- Limits impact of malicious actors or compromised credentials
- Ensures stable performance during peak usage
- Simple and transparent to end users (rate limit resets each minute)

---

## 11. Background Scheduled Jobs

### Task Lateness Scheduler

Service: `TaskSchedulerService`

Scheduled execution:

- **Cron**: `0 0 * * * *` (daily at midnight)
- **Task**: Automatically updates the `is_late` flag for all tasks

Logic:

```text
For each task in the database:
  if due_date < today AND status != TERMINE (COMPLETED):
    set is_late = true
  else:
    set is_late = false
```

Purpose:

- Ensures the `is_late` flag remains synchronized with the current date without manual intervention.
- Allows tasks to be marked as late automatically without requiring a status change.
- Optimized to only update tasks whose late status has changed.

## 12. Manual API Tests Already Performed

The following flow was manually tested with Postman/API calls:

1. Register user.
2. Login user.
3. Call `/auth/me`.
4. Create project.
5. List projects.
6. Create task in a project.
7. List tasks with pagination.
8. Filter tasks.
9. Update task status, priority, due date, and assignment.
10. Delete task.
11. Create a second user.
12. Create a project invitation for the second user.

Observed successful responses:

- Registration returned JWT and user data.
- Login returned JWT and user data.
- `/auth/me` returned authenticated user profile.
- Project creation returned project with owner ID and owner username.
- Project list returned a JSON array.
- Task creation returned task details.
- Paginated task list returned Spring Page JSON.
- Task update returned updated fields.
- Invitation creation returned invitation token and pending status.

## 13. Automated Tests

The backend contains unit/controller tests under:

```text
src/test/java/com/gestproj/backend
```

Tested modules:

- Auth controller
- Auth service
- Project controller
- Project service
- Task controller
- Task service
- Project member controller
- Project member service
- Project invitation controller
- Project invitation service
- Notification controller
- Notification service
- Spring context load test

Last test run:

```text
mvn test
```

Result:

```text
Tests run: 32
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

## 14. Suggested Diagrams to Generate

The cahier des charges should include at least these diagrams.

### Use Case Diagram

Actors:

- Visitor
- Authenticated User
- Project Owner
- Project Member

Use cases:

- Register
- Login
- View profile
- Create project
- View projects
- Update/delete project
- Create/update/delete task
- Assign task
- Invite member
- Accept/reject invitation
- Manage member permissions
- View notifications
- Mark notifications as read
- View activity logs

### Entity Relationship Diagram

Entities:

- User
- Project
- ProjectMember
- Task
- ProjectInvitation
- Notification
- ActivityLog

Relations:

- User owns many Projects.
- User belongs to many Projects through ProjectMember.
- Project has many ProjectMembers.
- Project has many Tasks.
- Task may be assigned to one User.
- Project has many Invitations.
- User has many Notifications.
- Project has many ActivityLogs.

### Class Diagram

Suggested classes:

- `User`, `Project`, `ProjectMember`, `Task`, `ProjectInvitation`, `Notification`, `ActivityLog`
- Services: `AuthService`, `UserService`, `ProjectService`, `TaskService`, `ProjectMemberService`, `ProjectInvitationService`, `NotificationService`, `ActivityLogService`
- Repositories for each entity
- Controllers for each module

### Sequence Diagram: Login

Flow:

1. Client sends email/password to `/auth/login`.
2. `AuthController` calls `AuthService`.
3. `AuthService` loads user by email.
4. Password is checked with BCrypt.
5. `JwtService` generates token.
6. API returns token and user response.

### Sequence Diagram: Create Project

Flow:

1. Client sends project creation request with JWT.
2. JWT filter authenticates user.
3. `ProjectController` calls `ProjectService`.
4. Project is saved.
5. Owner membership is created.
6. Activity log is created.
7. API returns project response.

### Sequence Diagram: Invite Member

Flow:

1. Owner sends invitation request.
2. Backend checks owner or invite permission.
3. Invitation token is generated.
4. Invitation is saved with `PENDING` status.
5. Notification is created if the invited email belongs to an existing user.
6. API returns invitation token.

### Sequence Diagram: Accept Invitation

Flow:

1. Invited user sends token to `/invites/{token}/accept`.
2. Backend validates token status and expiration.
3. Backend verifies invited email if set.
4. ProjectMember is created with proposed role and permissions.
5. Invitation status becomes `ACCEPTED`.
6. Notification is sent to inviter.

## 15. Important Business Rules

- A user can belong to several projects.
- A project has exactly one owner.
- Owner is automatically a project member.
- A user cannot have duplicate membership in the same project.
- Only owner or authorized members can invite users.
- Only owner or authorized members can manage members.
- Task assignment is allowed only to users who are members of the project.
- A task can be unassigned.
- Task lateness is separate from task status.
- Invitations expire after 7 days.
- Activity logs are chronological and should not be modified after creation.

## 16. Suggested Future Improvements

Potential improvements for the next iterations:

- Add refresh tokens.
- Add email sending for invitations.
- Add file upload for profile images.
- Add integration tests with Testcontainers.
- Add role-based annotations or policy layer for permissions.
- Add frontend integration.
- Add Docker configuration after the application is stable.
- Add CI pipeline for tests.

## 17. Prompt to Give Claude

Use this prompt:

```text
I am building a Spring Boot backend for a collaborative task management application. Based on the following project summary, generate a professional cahier des charges in French. Include: project context, objectives, functional requirements, non-functional requirements, user roles, business rules, technical architecture, database model, REST API description, security model, testing strategy, and proposed UML/Merise diagrams. Also generate detailed descriptions for: use case diagram, entity relationship diagram, class diagram, sequence diagrams for login, project creation, task creation, invitation creation, and invitation acceptance.

[Paste the full project summary here]
```

