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
- Database migrations: Flyway through Spring Boot Flyway starter
- Email delivery: Spring Boot Mail with SMTP configuration, recommended provider Resend
- Validation: Jakarta Bean Validation through Spring Boot validation starter
- Testing: JUnit 5, Mockito, Spring Boot Test, H2 in PostgreSQL compatibility mode for isolated context tests
- Local profile: `local`

Main Maven dependencies:

- `spring-boot-starter-webmvc`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `spring-boot-starter-actuator`
- `spring-boot-starter-mail`
- `springdoc-openapi-starter-webmvc-ui` (v2.8.9)
- `postgresql`
- `spring-boot-starter-flyway`
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson`
- `spring-boot-starter-test`
- `spring-boot-starter-security-test`
- `h2` for test profile only

## 3. Configuration

Main configuration file:

- `src/main/resources/application.properties`

Important values:

- Application name: `backend`
- Active profile: `local`
- JWT secret configured through `app.jwt.secret`
- JWT expiration: `86400000` ms, equal to 24 hours
- Frontend base URL configured through `app.frontend-url`
- Email sending controlled through `app.mail.enabled`
- Email sender configured through `app.mail.from`
- SMTP settings configured through `spring.mail.*`
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
- Sends an invitation email when email delivery is enabled and an invited email is provided.

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
src/main/resources/db/migration/V2__add_task_comments.sql
```

Deletion policy:

- When a project is deleted by its owner, project-scoped dependent data is deleted in a transaction before the project itself:
  - notifications linked to the project
  - activity logs linked to the project
  - project invitations
  - project tasks and task comments
  - project members
- Users are not deleted when a project is deleted.

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
- `/auth/me` is protected and requires a valid JWT.
- Normal project, task, comment, member, invitation, notification, and activity endpoints require authentication.
- Project access checks require an active project membership. Suspended or invited memberships are not treated as active access.

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
- Only active project members can view and create comments on tasks.
- Comments include: `id`, `taskId`, `userId`, `username`, `userEmail`, `content`, `createdAt`, `updatedAt`.
- Comments are returned in reverse chronological order (newest first).
- Only the comment author can delete their own comments.
- Deleting a comment verifies that the comment belongs to the `taskId` in the URL.
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

Behavior:

- Project owner or authorized users can create invitations.
- Invitations can be listed by project.
- Invited users can inspect, accept, or reject invitation tokens.
- If SMTP email is enabled, the backend sends an invitation email containing the frontend invitation link.
- Email delivery failure is logged and does not block invitation creation.

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
- **Task comments** with create/read/delete, permission checks, and reverse-chronological ordering
- Project invitations by email/token
- Optional SMTP email sending for project invitations
- Invitation accept/reject
- Notifications for invitation and member events
- Activity logs for important project actions
- Global exception handling for not found, conflict, unauthorized, and forbidden errors
- Flyway database migration
- Local PostgreSQL configuration
- Isolated test profile using H2 in PostgreSQL compatibility mode
- OpenAPI/Swagger documentation with interactive UI
- Actuator health and info endpoints for monitoring
- Scheduled job to automatically update task lateness flag
- **API rate limiting** (100 requests per minute per user for stability and security)
- Active-member access enforcement for project resources
- Safe project deletion with dependent project data cleanup

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

### 3. Task Comments (Create/Read/Delete with Permissions)

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
- Delete operations verify both the comment author and the task-comment relationship

**Permission Model**:
- Create: User must be an active project member (checked via `ProjectMemberService.isMember()`)
- Read: User must be an active project member
- Delete: Only the comment author can delete their own comment, and the comment must belong to the task in the URL
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
- A dedicated `RateLimitExceededException` is handled globally and mapped to HTTP 429
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

## 11. Recent Stabilization Fixes

After code review and testing, the following backend hardening fixes were applied:

- `/auth/me` was moved from public access to protected access.
- Rate limiting now returns HTTP `429 TOO_MANY_REQUESTS` instead of a forbidden response.
- Rate limit responses include a `Retry-After` header and `retryAfter` field.
- Project access now requires active membership. Invited or suspended membership records do not grant normal access.
- Owner member updates support partial updates where the role field is omitted.
- Task comment deletion verifies that the comment belongs to the task ID in the route.
- Project deletion now removes dependent project-scoped rows in a transaction before deleting the project.
- The lateness scheduler cron was corrected to daily execution at midnight.
- The test profile was changed to an isolated H2 database in PostgreSQL compatibility mode.
- Flyway is now wired through `spring-boot-starter-flyway`, allowing migrations to run automatically before schema validation in tests.

## 12. Background Scheduled Jobs

### Task Lateness Scheduler

Service: `TaskSchedulerService`

Scheduled execution:

- **Cron**: `0 0 0 * * *` (daily at midnight)
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

## 13. Manual API Tests Already Performed

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

## 13.1 Complete Postman Test Plan

This section describes the API tests to perform manually with Postman.

Base URL:

```text
http://localhost:8080
```

Recommended Postman environment variables:

```text
baseUrl=http://localhost:8080
token=
userId=
secondToken=
secondUserId=
projectId=
taskId=
commentId=
invitationToken=
memberId=
notificationId=
```

### 13.1.1 How to Configure Postman

Create a Postman environment named:

```text
GestProj Local
```

Add these variables:

| Variable | Initial value | Current value | Purpose |
| --- | --- | --- | --- |
| `baseUrl` | `http://localhost:8080` | `http://localhost:8080` | Backend API base URL |
| `token` | empty | empty | JWT token for the owner user |
| `userId` | empty | empty | Owner user ID |
| `secondToken` | empty | empty | JWT token for the second user |
| `secondUserId` | empty | empty | Second user ID |
| `projectId` | empty | empty | Created project ID |
| `taskId` | empty | empty | Created task ID |
| `commentId` | empty | empty | Created task comment ID |
| `invitationToken` | empty | empty | Invitation token returned by invitation creation |
| `memberId` | empty | empty | Project member ID for member update tests |
| `notificationId` | empty | empty | Notification ID for notification tests |

Select the `GestProj Local` environment in the top-right environment selector before running requests.

### 13.1.2 Collection Authorization Setup

Create a Postman collection named:

```text
GestProj Backend API
```

In the collection settings:

1. Open the `Authorization` tab.
2. Set `Type` to `Bearer Token`.
3. Set `Token` to:

```text
{{token}}
```

For most protected requests, set request authorization to:

```text
Inherit auth from parent
```

For requests that must be executed as the second user, override the request authorization:

```text
Type: Bearer Token
Token: {{secondToken}}
```

Public requests such as `/auth/register`, `/auth/login`, `/actuator/health`, and `/v3/api-docs` can use:

```text
No Auth
```

### 13.1.3 Required Headers

For JSON requests, add this header:

```text
Content-Type: application/json
```

For protected requests, Postman will send this automatically if collection authorization is configured:

```text
Authorization: Bearer {{token}}
```

### 13.1.4 Where the Token Comes From

The JWT token is returned by:

```text
POST {{baseUrl}}/auth/register
POST {{baseUrl}}/auth/login
```

The response shape is:

```json
{
  "token": "jwt-token-value",
  "user": {
    "id": 1,
    "email": "owner@example.com",
    "username": "owner",
    "name": "Project Owner",
    "profileImageUrl": null
  }
}
```

The value to save into Postman is:

```text
token
```

For the owner user, save:

```text
response.token      -> {{token}}
response.user.id    -> {{userId}}
```

For the second user, save:

```text
response.token      -> {{secondToken}}
response.user.id    -> {{secondUserId}}
```

### 13.1.5 Postman Scripts to Save Variables Automatically

In Postman, open a request, go to the `Tests` tab, and paste the relevant script.

Save owner login/register response:

```javascript
const json = pm.response.json();

pm.environment.set("token", json.token);
pm.environment.set("userId", json.user.id);
```

Save second user login/register response:

```javascript
const json = pm.response.json();

pm.environment.set("secondToken", json.token);
pm.environment.set("secondUserId", json.user.id);
```

Save project ID after creating a project:

```javascript
const json = pm.response.json();

pm.environment.set("projectId", json.id);
```

Save task ID after creating a task:

```javascript
const json = pm.response.json();

pm.environment.set("taskId", json.id);
```

Save comment ID after creating a comment:

```javascript
const json = pm.response.json();

pm.environment.set("commentId", json.id);
```

Save invitation token after creating an invitation:

```javascript
const json = pm.response.json();

pm.environment.set("invitationToken", json.token);
```

Save member ID from the members list:

```javascript
const json = pm.response.json();
const secondUserId = Number(pm.environment.get("secondUserId"));
const member = json.find(item => item.userId === secondUserId);

if (member) {
  pm.environment.set("memberId", member.id);
}
```

Save notification ID from the notification list:

```javascript
const json = pm.response.json();

if (json.length > 0) {
  pm.environment.set("notificationId", json[0].id);
}
```

### 13.1.6 Basic Status Code Test Script

For each request, you can add a basic status assertion in the `Tests` tab.

For successful `GET`, `PUT`, or `PATCH` requests:

```javascript
pm.test("Request succeeded", function () {
  pm.expect(pm.response.code).to.be.oneOf([200, 204]);
});
```

For successful creation requests:

```javascript
pm.test("Resource created", function () {
  pm.expect(pm.response.code).to.be.oneOf([200, 201]);
});
```

For validation errors:

```javascript
pm.test("Validation failed", function () {
  pm.expect(pm.response.code).to.eql(400);
});
```

For forbidden access:

```javascript
pm.test("Access forbidden", function () {
  pm.expect(pm.response.code).to.eql(403);
});
```

For conflict errors:

```javascript
pm.test("Conflict detected", function () {
  pm.expect(pm.response.code).to.eql(409);
});
```

### 13.1.7 Practical Token Workflow

Use this order when starting a fresh Postman session:

```text
1. Start backend on http://localhost:8080
2. Select the GestProj Local Postman environment
3. Register or login owner user
4. Save response token as {{token}}
5. Register or login second user
6. Save response token as {{secondToken}}
7. Run owner requests with collection auth using {{token}}
8. Run second-user invitation acceptance with {{secondToken}}
9. Continue the rest of the test plan
```

If a protected request returns `401` or `403`, check:

- the correct Postman environment is selected ;
- `{{token}}` has a value ;
- the request authorization is set to `Bearer Token` or `Inherit auth from parent` ;
- the token is not copied with extra quotes ;
- the user has the required project permission.

Headers for protected endpoints:

```text
Authorization: Bearer {{token}}
Content-Type: application/json
```

For requests performed as the second user, use:

```text
Authorization: Bearer {{secondToken}}
Content-Type: application/json
```

### 1. Health Check

Request:

```text
GET {{baseUrl}}/actuator/health
```

Expected result:

```text
HTTP 200 OK
status = UP
```

### 2. Swagger/OpenAPI Check

Request:

```text
GET {{baseUrl}}/v3/api-docs
```

Expected result:

```text
HTTP 200 OK
JSON OpenAPI document
```

Swagger UI:

```text
GET {{baseUrl}}/swagger-ui.html
```

Expected result:

```text
HTTP 200 OK
Swagger UI page loads in browser
```

### 3. Register First User

Request:

```text
POST {{baseUrl}}/auth/register
```

Body:

```json
{
  "email": "owner@example.com",
  "username": "owner",
  "password": "Password123!",
  "name": "Project Owner"
}
```

Expected result:

```text
HTTP 201 Created or HTTP 200 OK
Response contains JWT token and user data
```

Postman action:

```text
Save response token as {{token}}
Save response user id as {{userId}}
```

### 4. Login First User

Request:

```text
POST {{baseUrl}}/auth/login
```

Body:

```json
{
  "email": "owner@example.com",
  "password": "Password123!"
}
```

Expected result:

```text
HTTP 200 OK
Response contains JWT token
```

Postman action:

```text
Save response token as {{token}}
```

### 5. Get Current User

Request:

```text
GET {{baseUrl}}/auth/me
```

Expected result:

```text
HTTP 200 OK
Response contains owner@example.com
```

Negative test:

```text
Call without Authorization header
Expected: HTTP 401 Unauthorized or 403 Forbidden depending on Spring Security response
```

### 6. Register Second User

Request:

```text
POST {{baseUrl}}/auth/register
```

Body:

```json
{
  "email": "member@example.com",
  "username": "member",
  "password": "Password123!",
  "name": "Project Member"
}
```

Expected result:

```text
HTTP 201 Created or HTTP 200 OK
Response contains JWT token and user data
```

Postman action:

```text
Save response token as {{secondToken}}
Save response user id as {{secondUserId}}
```

### 7. Search Users

Request:

```text
GET {{baseUrl}}/users/search?query=member
```

Expected result:

```text
HTTP 200 OK
Response contains member@example.com
```

### 8. Create Project

Request:

```text
POST {{baseUrl}}/projects
```

Body:

```json
{
  "name": "Alpha Project",
  "description": "Project used for backend API testing"
}
```

Expected result:

```text
HTTP 201 Created or HTTP 200 OK
Response contains project id, name, owner id, and owner username
```

Postman action:

```text
Save project id as {{projectId}}
```

### 9. List Projects

Request:

```text
GET {{baseUrl}}/projects
```

Expected result:

```text
HTTP 200 OK
Response contains Alpha Project
```

### 10. Get Project by ID

Request:

```text
GET {{baseUrl}}/projects/{{projectId}}
```

Expected result:

```text
HTTP 200 OK
Response contains project details
```

### 11. Update Project

Request:

```text
PUT {{baseUrl}}/projects/{{projectId}}
```

Body:

```json
{
  "name": "Alpha Project Updated",
  "description": "Updated project description"
}
```

Expected result:

```text
HTTP 200 OK
Response contains updated project name and description
```

### 12. Project Statistics

Request:

```text
GET {{baseUrl}}/projects/{{projectId}}/statistics
```

Expected result before creating tasks:

```text
HTTP 200 OK
totalTasks = 0
completionPercentage = 0
```

### 13. Create Task

Request:

```text
POST {{baseUrl}}/projects/{{projectId}}/tasks
```

Body:

```json
{
  "title": "Prepare backend tests",
  "description": "Create and validate Postman API tests",
  "status": "A_FAIRE",
  "priority": "HAUTE",
  "dueDate": "2026-07-20",
  "assignedToId": null
}
```

Expected result:

```text
HTTP 201 Created or HTTP 200 OK
Response contains task id, title, status, priority, project id
```

Postman action:

```text
Save task id as {{taskId}}
```

### 14. List Tasks with Pagination

Request:

```text
GET {{baseUrl}}/projects/{{projectId}}/tasks?page=0&size=10
```

Expected result:

```text
HTTP 200 OK
Response is a Spring Page JSON
content contains created task
```

### 15. Filter Tasks by Status

Request:

```text
GET {{baseUrl}}/projects/{{projectId}}/tasks?status=A_FAIRE
```

Expected result:

```text
HTTP 200 OK
Response contains tasks with status A_FAIRE
```

### 16. Filter Tasks by Priority

Request:

```text
GET {{baseUrl}}/projects/{{projectId}}/tasks?priority=HAUTE
```

Expected result:

```text
HTTP 200 OK
Response contains tasks with priority HAUTE
```

### 17. Filter Overdue Tasks

Request:

```text
GET {{baseUrl}}/projects/{{projectId}}/tasks?overdue=true
```

Expected result:

```text
HTTP 200 OK
Response contains only tasks marked late, or an empty page if none are late
```

### 18. Get Task by ID

Request:

```text
GET {{baseUrl}}/tasks/{{taskId}}
```

Expected result:

```text
HTTP 200 OK
Response contains task details
```

### 19. Update Task

Request:

```text
PUT {{baseUrl}}/tasks/{{taskId}}
```

Body:

```json
{
  "title": "Prepare backend tests updated",
  "description": "Validate all important API flows in Postman",
  "status": "EN_COURS",
  "priority": "MOYENNE",
  "dueDate": "2026-07-22",
  "assignedToId": null
}
```

Expected result:

```text
HTTP 200 OK
Response contains updated task values
```

### 20. Create Task Comment

Request:

```text
POST {{baseUrl}}/tasks/{{taskId}}/comments
```

Body:

```json
{
  "content": "This task was tested from Postman."
}
```

Expected result:

```text
HTTP 201 Created or HTTP 200 OK
Response contains comment id, task id, user id, username, content, createdAt
```

Postman action:

```text
Save comment id as {{commentId}}
```

### 21. List Task Comments

Request:

```text
GET {{baseUrl}}/tasks/{{taskId}}/comments
```

Expected result:

```text
HTTP 200 OK
Response contains the created comment
Comments are ordered newest first
```

### 22. Delete Task Comment

Request:

```text
DELETE {{baseUrl}}/tasks/{{taskId}}/comments/{{commentId}}
```

Expected result:

```text
HTTP 204 No Content or HTTP 200 OK
Comment is removed
```

Negative test:

```text
Try deleting the same comment again
Expected: HTTP 404 Not Found
```

### 23. Create Invitation for Second User

Request:

```text
POST {{baseUrl}}/projects/{{projectId}}/invites
```

Body:

```json
{
  "invitedEmail": "member@example.com",
  "proposedRole": "MEMBER",
  "roleTitle": "Developer",
  "roleDescription": "Works on project tasks",
  "canViewProject": true,
  "canCreateTask": true,
  "canEditTask": true,
  "canDeleteTask": false,
  "canInviteMember": false,
  "canManageMembers": false
}
```

Expected result:

```text
HTTP 201 Created or HTTP 200 OK
Response contains invitation token and PENDING status
If MAIL_ENABLED=true, an invitation email is attempted
If MAIL_ENABLED=false, invitation is still created without email
```

Postman action:

```text
Save token as {{invitationToken}}
```

### 24. List Project Invitations

Request:

```text
GET {{baseUrl}}/projects/{{projectId}}/invites
```

Expected result:

```text
HTTP 200 OK
Response contains pending invitation
```

### 25. Get Invitation by Token

Request as an authenticated project member or authorized user:

```text
GET {{baseUrl}}/invites/{{invitationToken}}
```

Expected result:

```text
HTTP 200 OK
Response contains invitation details
```

### 26. Accept Invitation as Second User

Use second user token:

```text
Authorization: Bearer {{secondToken}}
```

Request:

```text
POST {{baseUrl}}/invites/{{invitationToken}}/accept
```

Expected result:

```text
HTTP 200 OK
Invitation status becomes ACCEPTED
Second user becomes an ACTIVE project member
Owner receives notification
```

### 27. List Members

Use owner token again:

```text
Authorization: Bearer {{token}}
```

Request:

```text
GET {{baseUrl}}/projects/{{projectId}}/members
```

Expected result:

```text
HTTP 200 OK
Response contains owner and second user
```

Postman action:

```text
Save second user's member id as {{memberId}}
```

### 28. Update Member Permissions

Request:

```text
PUT {{baseUrl}}/projects/{{projectId}}/members/{{memberId}}
```

Body:

```json
{
  "role": "MEMBER",
  "status": "ACTIVE",
  "roleTitle": "Backend Developer",
  "roleDescription": "Can create and edit backend tasks",
  "canViewProject": true,
  "canCreateTask": true,
  "canEditTask": true,
  "canDeleteTask": false,
  "canInviteMember": false,
  "canManageMembers": false
}
```

Expected result:

```text
HTTP 200 OK
Response contains updated member permissions
```

### 29. Assign Task to Second User

Request:

```text
PUT {{baseUrl}}/tasks/{{taskId}}
```

Body:

```json
{
  "title": "Prepare backend tests updated",
  "description": "Assigned to member",
  "status": "EN_COURS",
  "priority": "MOYENNE",
  "dueDate": "2026-07-22",
  "assignedToId": "{{secondUserId}}"
}
```

Expected result:

```text
HTTP 200 OK
Response contains assigned user information
```

If Postman sends `assignedToId` as a string and the API expects a number, use the raw numeric value instead of `{{secondUserId}}` inside quotes.

### 30. Notifications

Request:

```text
GET {{baseUrl}}/notifications
```

Expected result:

```text
HTTP 200 OK
Response contains notifications for the authenticated user
```

Request:

```text
GET {{baseUrl}}/notifications/unread
```

Expected result:

```text
HTTP 200 OK
Response contains unread notifications
```

Postman action:

```text
Save a notification id as {{notificationId}}
```

### 31. Mark Notification as Read

Request:

```text
PATCH {{baseUrl}}/notifications/{{notificationId}}/read
```

Expected result:

```text
HTTP 200 OK
Notification read field becomes true
```

### 32. Mark All Notifications as Read

Request:

```text
PATCH {{baseUrl}}/notifications/read-all
```

Expected result:

```text
HTTP 200 OK or HTTP 204 No Content
All notifications for the authenticated user are marked as read
```

### 33. Activity Logs

Request:

```text
GET {{baseUrl}}/projects/{{projectId}}/activity-logs
```

Expected result:

```text
HTTP 200 OK
Response contains chronological project activity logs
```

### 34. Updated Project Statistics

Request:

```text
GET {{baseUrl}}/projects/{{projectId}}/statistics
```

Expected result after task creation/update:

```text
HTTP 200 OK
totalTasks >= 1
inProgressTasks >= 1 if the task status is EN_COURS
completionPercentage is calculated from completed tasks
```

### 35. Complete Task

Request:

```text
PUT {{baseUrl}}/tasks/{{taskId}}
```

Body:

```json
{
  "title": "Prepare backend tests updated",
  "description": "Task completed",
  "status": "TERMINE",
  "priority": "MOYENNE",
  "dueDate": "2026-07-22",
  "assignedToId": "{{secondUserId}}"
}
```

Expected result:

```text
HTTP 200 OK
Task status becomes TERMINE
```

### 36. Delete Task

Request:

```text
DELETE {{baseUrl}}/tasks/{{taskId}}
```

Expected result:

```text
HTTP 204 No Content or HTTP 200 OK
Task is deleted
```

Negative test:

```text
GET {{baseUrl}}/tasks/{{taskId}}
Expected: HTTP 404 Not Found
```

### 37. Create and Reject Invitation

Create another invitation for a different email:

```text
POST {{baseUrl}}/projects/{{projectId}}/invites
```

Body:

```json
{
  "invitedEmail": "reject-user@example.com",
  "proposedRole": "MEMBER",
  "roleTitle": "Tester",
  "roleDescription": "Temporary tester",
  "canViewProject": true,
  "canCreateTask": false,
  "canEditTask": false,
  "canDeleteTask": false,
  "canInviteMember": false,
  "canManageMembers": false
}
```

Save the returned token, then reject:

```text
POST {{baseUrl}}/invites/{{invitationToken}}/reject
```

Expected result:

```text
HTTP 200 OK
Invitation status becomes REJECTED
```

### 38. Forbidden Access Test

Use the second user token after setting the member as suspended or without the required permission.

Example request:

```text
POST {{baseUrl}}/projects/{{projectId}}/invites
```

Expected result:

```text
HTTP 403 Forbidden
User does not have invite permission
```

### 39. Duplicate Membership Test

Try inviting a user who is already an active project member:

```text
POST {{baseUrl}}/projects/{{projectId}}/invites
```

Body:

```json
{
  "invitedEmail": "member@example.com",
  "proposedRole": "MEMBER",
  "roleTitle": "Developer",
  "roleDescription": "Already a member",
  "canViewProject": true,
  "canCreateTask": true,
  "canEditTask": true,
  "canDeleteTask": false,
  "canInviteMember": false,
  "canManageMembers": false
}
```

Expected result:

```text
HTTP 409 Conflict
Message indicates the user is already a project member
```

### 40. Validation Error Test

Request:

```text
POST {{baseUrl}}/projects/{{projectId}}/invites
```

Body:

```json
{
  "invitedEmail": "not-an-email",
  "proposedRole": "MEMBER",
  "roleTitle": "Tester",
  "roleDescription": "Invalid email test",
  "canViewProject": true,
  "canCreateTask": false,
  "canEditTask": false,
  "canDeleteTask": false,
  "canInviteMember": false,
  "canManageMembers": false
}
```

Expected result:

```text
HTTP 400 Bad Request
Validation error for invitedEmail
```

### 41. Rate Limit Test

Send more than 100 requests in one minute with the same user token or same IP.

Expected result:

```text
HTTP 429 Too Many Requests
Response includes Retry-After header and retryAfter field
```

### 42. Delete Project

Request:

```text
DELETE {{baseUrl}}/projects/{{projectId}}
```

Expected result:

```text
HTTP 204 No Content or HTTP 200 OK
Project is deleted
Project-scoped tasks, comments, invitations, notifications, members, and activity logs are cleaned up
Users are not deleted
```

Negative test:

```text
GET {{baseUrl}}/projects/{{projectId}}
Expected: HTTP 404 Not Found or HTTP 403 Forbidden depending on access handling
```

### Recommended Postman Execution Order

Run the tests in this order:

```text
1. Health and Swagger checks
2. Register/login owner
3. Register/login second user
4. Create project
5. Create/list/update tasks
6. Create/list/delete comments
7. Create invitation
8. Accept invitation as second user
9. List/update members
10. Assign task to second user
11. Check notifications and activity logs
12. Test negative cases
13. Delete project at the end
```

## 14. Automated Tests

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
- Rate limit interceptor
- Task comment service
- Spring context load test

Test profile:

- Uses H2 in PostgreSQL compatibility mode.
- Runs Flyway migrations from `classpath:db/migration`.
- Validates the JPA schema with Hibernate after migrations.
- Does not depend on the local PostgreSQL database state.

Last test run after stabilization fixes:

```text
mvn test
```

Result:

```text
Tests run: 36
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

## 14.1 Test and Verification Commands Used During Development

The following commands were used to verify the backend during the improvement and documentation work.

### Run the Full Automated Test Suite

Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Purpose:

- Compiles the backend.
- Runs all JUnit/Mockito/Spring Boot tests.
- Runs the Spring context load test.
- Runs Flyway migrations in the test profile.
- Validates the JPA schema against the H2 PostgreSQL-compatible test database.

Latest observed result:

```text
Tests run: 36
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Linux/macOS equivalent:

```bash
./mvnw test
```

### Validate Docker Compose Configuration

Command attempted:

```powershell
docker compose config
```

Purpose:

- Validates the `docker-compose.yml` syntax.
- Resolves and prints the final Compose configuration.

Observed result in this environment:

```text
docker : The term 'docker' is not recognized as the name of a cmdlet, function, script file, or operable program.
```

Conclusion:

- Docker was not installed or not available on PATH in the current environment.
- The command should be rerun after installing Docker Desktop or making Docker available on PATH.

### Check Git Working Tree Status

Command:

```powershell
git status --short
```

Purpose:

- Shows modified and newly created files.
- Helps separate current work from previous uncommitted changes.

### Review Documentation Changes

Command:

```powershell
git diff -- docs/backend-summary-for-cahier-des-charges.md
```

Purpose:

- Shows the exact documentation changes made in the project summary/cahier des charges file.

### Review Changed File Summary

Command:

```powershell
git diff --stat
```

Purpose:

- Shows a compact summary of changed files and line counts.

### Read the Documentation File

Command:

```powershell
Get-Content -Path docs/backend-summary-for-cahier-des-charges.md
```

Purpose:

- Reads the full documentation file.

Tail view:

```powershell
Get-Content -Path docs/backend-summary-for-cahier-des-charges.md -Tail 80
```

Purpose:

- Reads the last 80 lines of the documentation file.

UTF-8 verification:

```powershell
Get-Content -Path docs/backend-summary-for-cahier-des-charges.md -Encoding UTF8 -Tail 20
```

Purpose:

- Confirms French accents render correctly when the file is read as UTF-8.

### Search Inside the Project

Command:

```powershell
rg --files src/main src/test
```

Purpose:

- Lists source and test files quickly.

Command:

```powershell
rg -n "Postman|Manual API Tests|Automated Tests" docs/backend-summary-for-cahier-des-charges.md
```

Purpose:

- Finds documentation sections related to manual and automated tests.

### Inspect Important Source Files

Commands:

```powershell
Get-Content -Path src/main/java/com/gestproj/backend/auth/dto/AuthResponse.java
Get-Content -Path src/main/java/com/gestproj/backend/user/dto/UserResponse.java
Get-Content -Path src/main/java/com/gestproj/backend/project/dto/ProjectResponse.java
Get-Content -Path src/main/java/com/gestproj/backend/task/dto/TaskResponse.java
```

Purpose:

- Confirms the response JSON structure used by Postman scripts.
- Verifies where fields such as `token`, `user.id`, `project.id`, and `task.id` come from.

### Recommended Commands for Future Backend Validation

Run these before considering a backend change complete:

```powershell
.\mvnw.cmd test
git status --short
git diff --stat
```

If Docker is installed:

```powershell
docker compose config
docker compose up --build
```

After starting the backend:

```text
GET http://localhost:8080/actuator/health
GET http://localhost:8080/v3/api-docs
GET http://localhost:8080/swagger-ui.html
```

## 15. Suggested Diagrams to Generate

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

## 16. Important Business Rules

- A user can belong to several projects.
- A project has exactly one owner.
- Owner is automatically a project member.
- A user cannot have duplicate membership in the same project.
- Only active members can access project resources.
- Suspended or invited members do not have normal project access.
- Only owner or authorized members can invite users.
- Only owner or authorized members can manage members.
- Task assignment is allowed only to users who are active members of the project.
- A task can be unassigned.
- Task lateness is separate from task status.
- Invitations expire after 7 days.
- Activity logs are chronological and should not be modified after creation.
- Only the author of a task comment can delete it.
- Deleting a project deletes project-scoped data but does not delete users.

## 17. Suggested Future Improvements

Potential improvements for the next iterations:

- Add refresh tokens.
- Add file upload for profile images.
- Add integration tests with Testcontainers for real PostgreSQL behavior.
- Add role-based annotations or policy layer for permissions.
- Add frontend integration.
- Add CI pipeline for tests.
- Add project archiving as an alternative to hard delete.
- Add comment update/edit history if team discussion needs full edit support.
- Add database indexes for common task filters if performance becomes important.
- Add task labels/tags such as Backend, Frontend, Bug, Urgent, Design, and Testing.
- Add due date reminders for tasks due today, due tomorrow, or overdue.
- Add a task activity timeline showing status changes, priority changes, assignments, and comments.
- Add richer project dashboard metrics such as tasks by priority, tasks by assignee, overdue tasks, and recent activity.
- Add user profile update features for name, username, password, and profile image URL.
- Add task attachments or external links for screenshots, documents, and specifications.
- Add notification preferences so users can choose which events should notify them.

Recommended priority for user experience improvements:

```text
1. Due date reminders
2. Task labels/tags
3. Task activity timeline
4. User profile update
5. Project archive/unarchive
6. Task attachments
7. Notification preferences
```

### Suggested Email Provider

For this project, the recommended email provider is **Resend**.

Reasons:

- It is simple to integrate for transactional emails.
- It supports both SMTP and API-based sending.
- It is suitable for invitation emails, verification emails, reminders, and notification emails.
- Using SMTP with Spring Boot keeps the backend implementation provider-independent through `spring-boot-starter-mail`.

Recommended backend approach:

```text
Use Spring Boot Mail + Resend SMTP
```

Suggested environment variables:

```text
MAIL_ENABLED=true
MAIL_HOST=smtp.resend.com
MAIL_PORT=587
MAIL_USERNAME=resend
MAIL_PASSWORD=<resend-api-key>
MAIL_FROM=no-reply@your-domain.com
APP_FRONTEND_URL=http://localhost:3000
```

Alternative providers:

- SendGrid: mature and widely used, with Java API support.
- Mailgun: powerful for larger email workflows and domain-based sending.
- Mailtrap: useful for development and testing email delivery without sending to real users.

For a student project or first production-like version, Resend SMTP is the cleanest choice because it keeps the implementation simple while still using a real transactional email service. This recommendation has been used in the backend implementation through Spring Boot Mail and SMTP environment variables.

## 18. Prompt to Give Claude

Use this prompt:

```text
I am building a Spring Boot backend for a collaborative task management application. Based on the following project summary, generate a professional cahier des charges in French. Include: project context, objectives, functional requirements, non-functional requirements, user roles, business rules, technical architecture, database model, REST API description, security model, testing strategy, and proposed UML/Merise diagrams. Also generate detailed descriptions for: use case diagram, entity relationship diagram, class diagram, sequence diagrams for login, project creation, task creation, invitation creation, and invitation acceptance.

[Paste the full project summary here]
```

---

## 18.1 Backend Improvements Added After Documentation Draft

The backend was improved with environment-based runtime configuration and Docker Compose support:

- `spring.profiles.active` can now be controlled through `SPRING_PROFILES_ACTIVE`, with `local` as the default.
- JWT configuration can now be controlled through `JWT_SECRET` and `JWT_EXPIRATION_MS`.
- Local PostgreSQL settings can now be overridden through `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`.
- A dedicated `docker` profile was added in `application-docker.properties`.
- `docker-compose.yml` was added to run the backend with PostgreSQL 15.
- `.dockerignore` was added to keep Docker builds smaller and avoid copying local/generated folders.
- `README.md` now documents local startup, Docker Compose startup, Swagger URL, and test command.
- Maven Surefire now loads Mockito as a Java agent explicitly, avoiding the dynamic agent loading warning on modern JDKs.
- `spring-boot-starter-mail` was added for SMTP email delivery.
- `EmailService` was added to send project invitation emails when mail is enabled.
- `ProjectInvitationService` now triggers email delivery after invitation creation.
- Mail delivery is optional and controlled by environment variables, so local development is not blocked by SMTP configuration.
- Invitation email failures are logged but do not roll back invitation creation.

Verification:

```text
.\mvnw.cmd test

Tests run: 36
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Docker Compose syntax validation could not be executed in this environment because the `docker` command is not installed or not available on PATH.

---

# Cahier des Charges - Backend GestProj

## 19. Présentation Générale du Projet

### 19.1 Contexte

Le projet GestProj consiste à développer une application web de gestion collaborative de projets et de tâches. L'objectif principal est de fournir une plateforme permettant à plusieurs utilisateurs de travailler ensemble sur des projets, de suivre l'avancement des tâches, de gérer les membres, les invitations, les notifications et l'historique des actions importantes.

Ce document décrit principalement la partie backend de l'application. Le backend expose une API REST sécurisée, destinée à être consommée par une interface frontend ou par tout autre client compatible HTTP.

### 19.2 Problématique

Dans un environnement de travail collaboratif, les équipes ont besoin d'un outil centralisé pour :

- organiser les projets ;
- créer, assigner et suivre les tâches ;
- inviter et gérer les membres ;
- contrôler les droits d'accès ;
- consulter les notifications importantes ;
- garder une trace des actions effectuées.

Sans solution centralisée, le suivi devient dispersé entre plusieurs outils, ce qui complique la coordination, la traçabilité et la visibilité sur l'avancement du travail.

### 19.3 Objectifs du Projet

Les objectifs du backend sont :

- fournir une API REST fiable pour la gestion de projets collaboratifs ;
- sécuriser l'accès aux ressources avec une authentification JWT ;
- gérer les utilisateurs, projets, membres, tâches, invitations, notifications et journaux d'activité ;
- appliquer des règles d'accès selon le rôle et les permissions des membres ;
- assurer la persistance des données dans une base PostgreSQL ;
- documenter les endpoints avec Swagger/OpenAPI ;
- fournir une base technique stable, testable et extensible.

## 20. Périmètre du Projet

### 20.1 Fonctionnalités Incluses

Le backend couvre les fonctionnalités suivantes :

- inscription et connexion des utilisateurs ;
- authentification stateless par JWT ;
- consultation du profil de l'utilisateur connecté ;
- recherche d'utilisateurs par email, nom d'utilisateur ou nom complet ;
- création, consultation, modification et suppression de projets ;
- création automatique du membre propriétaire lors de la création d'un projet ;
- gestion des membres d'un projet ;
- gestion des rôles, statuts et permissions des membres ;
- création, consultation, modification, suppression et filtrage des tâches ;
- assignation d'une tâche à un membre actif du projet ;
- gestion des commentaires sur les tâches ;
- création et gestion des invitations par email et jeton ;
- acceptation ou refus d'une invitation ;
- création et consultation des notifications ;
- marquage des notifications comme lues ;
- consultation des journaux d'activité ;
- calcul des statistiques d'un projet ;
- limitation du débit des requêtes API ;
- mise à jour automatique de l'état de retard des tâches ;
- documentation interactive de l'API via Swagger UI.

### 20.2 Fonctionnalités Hors Périmètre Actuel

Les éléments suivants sont identifiés comme améliorations futures :

- refresh tokens ;
- upload de fichiers ou d'images de profil ;
- intégration frontend complète ;
- déploiement Docker final ;
- pipeline CI/CD ;
- tests d'intégration avec Testcontainers ;
- archivage de projets ;
- historique d'édition des commentaires.
- étiquettes de tâches ;
- rappels d'échéance ;
- historique détaillé par tâche ;
- pièces jointes ou liens externes sur les tâches ;
- préférences de notifications.

## 21. Acteurs du Système

### 21.1 Visiteur

Un visiteur est une personne non authentifiée. Il peut uniquement :

- créer un compte ;
- se connecter ;
- consulter les endpoints publics de santé et documentation API.

### 21.2 Utilisateur Authentifié

Un utilisateur authentifié possède un compte valide et un token JWT. Il peut :

- consulter son profil ;
- rechercher d'autres utilisateurs ;
- créer un projet ;
- consulter les projets dont il est membre ;
- recevoir des notifications ;
- accepter ou refuser une invitation.

### 21.3 Propriétaire de Projet

Le propriétaire est l'utilisateur qui crée le projet. Il dispose des droits les plus élevés sur ce projet. Il peut :

- modifier ou supprimer le projet ;
- inviter des membres ;
- gérer les membres ;
- modifier les rôles, statuts et permissions ;
- créer, modifier et supprimer les tâches selon les règles du projet ;
- consulter les statistiques et les journaux d'activité.

### 21.4 Membre de Projet

Un membre de projet est un utilisateur associé à un projet. Ses actions dépendent de son statut et de ses permissions :

- consulter le projet si son statut est `ACTIVE` ;
- créer des tâches si `can_create_task` est vrai ;
- modifier des tâches si `can_edit_task` est vrai ;
- supprimer des tâches si `can_delete_task` est vrai ;
- inviter des membres si `can_invite_member` est vrai ;
- gérer les membres si `can_manage_members` est vrai ;
- commenter les tâches du projet.

## 22. Exigences Fonctionnelles

### 22.1 Authentification

Le système doit permettre à un utilisateur de s'inscrire avec un email, un nom d'utilisateur, un mot de passe et un nom complet.

Le système doit hacher le mot de passe avec BCrypt avant l'enregistrement.

Le système doit permettre à un utilisateur de se connecter avec ses identifiants.

Après une connexion réussie, le système doit retourner un token JWT et les informations de l'utilisateur.

Le système doit permettre à un utilisateur authentifié de consulter son profil via `/auth/me`.

### 22.2 Gestion des Utilisateurs

Le système doit permettre de consulter un utilisateur par identifiant.

Le système doit permettre de rechercher des utilisateurs par email, username ou nom.

La création directe d'un utilisateur par `/users` n'est pas autorisée. Toute création d'utilisateur doit passer par `/auth/register`.

### 22.3 Gestion des Projets

Le système doit permettre à un utilisateur authentifié de créer un projet.

Lors de la création d'un projet, le système doit :

- enregistrer le projet ;
- définir l'utilisateur connecté comme propriétaire ;
- créer automatiquement une entrée `ProjectMember` avec le rôle `OWNER` ;
- enregistrer une activité de création.

Le système doit permettre à un utilisateur de consulter uniquement les projets dont il est membre.

Le système doit permettre de modifier ou supprimer un projet selon les droits de l'utilisateur.

La suppression d'un projet doit supprimer les données dépendantes liées au projet sans supprimer les utilisateurs.

### 22.4 Statistiques de Projet

Le système doit fournir des statistiques pour chaque projet :

- nombre total de tâches ;
- nombre de tâches terminées ;
- nombre de tâches en cours ;
- nombre de tâches non commencées ;
- nombre de tâches en retard ;
- pourcentage de complétion.

Ces statistiques doivent être calculées à partir des tâches existantes du projet.

### 22.5 Gestion des Tâches

Le système doit permettre de créer une tâche dans un projet.

Une tâche doit contenir au minimum un titre et être rattachée à un projet.

Une tâche peut contenir :

- une description ;
- un statut ;
- une priorité ;
- une date limite ;
- un utilisateur assigné.

Le système doit permettre de consulter, modifier et supprimer une tâche selon les permissions de l'utilisateur.

Le système doit permettre de filtrer les tâches par :

- statut ;
- priorité ;
- utilisateur assigné ;
- état de retard.

Le système doit permettre une pagination sur la liste des tâches.

### 22.6 Commentaires sur les Tâches

Le système doit permettre aux membres actifs d'un projet de commenter les tâches.

Le système doit permettre de consulter les commentaires d'une tâche dans l'ordre antéchronologique.

Seul l'auteur d'un commentaire peut supprimer son propre commentaire.

Lors d'une suppression, le système doit vérifier que le commentaire appartient bien à la tâche mentionnée dans l'URL.

### 22.7 Gestion des Invitations

Le système doit permettre à un propriétaire ou à un membre autorisé d'inviter un utilisateur à rejoindre un projet.

L'invitation doit contenir :

- le projet concerné ;
- l'utilisateur invitant ;
- l'email invité ;
- un token unique ;
- un statut ;
- une date d'expiration ;
- les rôles et permissions proposés.

Le système doit permettre à l'utilisateur invité d'accepter ou de refuser l'invitation.

Une invitation expirée ou déjà traitée ne doit pas pouvoir être acceptée.

### 22.8 Gestion des Membres

Le système doit permettre de lister les membres d'un projet.

Le système doit permettre au propriétaire ou à un membre autorisé de modifier :

- le rôle ;
- le statut ;
- le titre fonctionnel ;
- la description du rôle ;
- les permissions.

Un membre suspendu ou simplement invité ne doit pas accéder aux ressources normales du projet.

### 22.9 Notifications

Le système doit créer des notifications pour les événements importants, notamment :

- invitation envoyée ;
- invitation acceptée ;
- invitation refusée ;
- membre mis à jour ;
- statut de membre modifié.

Le système doit permettre à un utilisateur de consulter toutes ses notifications.

Le système doit permettre de consulter uniquement les notifications non lues.

Le système doit permettre de marquer une notification comme lue ou de marquer toutes les notifications comme lues.

### 22.10 Journaux d'Activité

Le système doit conserver un journal des actions importantes effectuées dans un projet.

Les journaux doivent être consultables par les membres autorisés du projet.

Les actions journalisées incluent :

- création de projet ;
- création de tâche ;
- modification de tâche ;
- suppression de tâche ;
- création d'invitation ;
- acceptation d'invitation ;
- modification d'un membre.

## 23. Exigences Non Fonctionnelles

### 23.1 Sécurité

L'API doit être sécurisée avec Spring Security.

L'authentification doit être basée sur JWT.

Les sessions serveur ne doivent pas être utilisées.

Les mots de passe doivent être hachés avec BCrypt.

Les endpoints protégés doivent refuser les requêtes sans token valide.

Les permissions doivent être vérifiées côté backend.

### 23.2 Performance

Le système doit supporter la pagination pour les listes potentiellement volumineuses, notamment les tâches.

Le système doit limiter les requêtes à 100 requêtes par minute et par utilisateur ou adresse IP.

Les recherches et filtres doivent être conçus pour pouvoir évoluer avec des index de base de données si nécessaire.

### 23.3 Fiabilité

Les migrations de base de données doivent être gérées par Flyway.

Le schéma de base de données doit être validé par Hibernate.

Les suppressions sensibles, comme la suppression d'un projet, doivent être transactionnelles.

Les erreurs doivent être gérées par un gestionnaire global d'exceptions.

### 23.4 Maintenabilité

Le code doit suivre une architecture par fonctionnalité.

Chaque fonctionnalité doit regrouper ses contrôleurs, services, DTO, entités et repositories.

Le backend doit être couvert par des tests unitaires et des tests de contexte Spring.

L'API doit être documentée avec OpenAPI/Swagger.

### 23.5 Portabilité

Le projet doit pouvoir être exécuté localement avec PostgreSQL.

Le profil de test doit utiliser H2 en mode compatibilité PostgreSQL.

La configuration doit permettre une future conteneurisation avec Docker.

## 24. Architecture Technique

### 24.1 Vue d'Ensemble

Le backend est construit avec Spring Boot et suit une architecture en couches :

- couche contrôleur : réception des requêtes HTTP ;
- couche service : logique métier et règles de sécurité applicatives ;
- couche repository : accès aux données avec Spring Data JPA ;
- couche entité : représentation du modèle persistant ;
- couche DTO : objets d'entrée et de sortie de l'API ;
- couche configuration : sécurité, JWT, CORS, rate limiting et documentation.

### 24.2 Technologies Utilisées

| Élément | Technologie |
| --- | --- |
| Langage | Java 21 |
| Framework | Spring Boot 4.1.0 |
| API | REST |
| Build | Maven |
| Sécurité | Spring Security, JWT |
| Hash mot de passe | BCrypt |
| Base de données | PostgreSQL 15 |
| ORM | Spring Data JPA / Hibernate |
| Migrations | Flyway |
| Validation | Jakarta Bean Validation |
| Documentation API | Springdoc OpenAPI / Swagger UI |
| Tests | JUnit 5, Mockito, Spring Boot Test |
| Base de test | H2 en mode PostgreSQL |

### 24.3 Organisation du Code

Le code est organisé autour des fonctionnalités métier :

```text
com.gestproj.backend
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

Cette organisation facilite la navigation, la maintenance et l'évolution du projet.

## 25. Modèle de Données

### 25.1 Entités Principales

Les entités principales du système sont :

- `User` : utilisateur inscrit ;
- `Project` : espace collaboratif ;
- `ProjectMember` : relation entre utilisateur et projet avec rôle et permissions ;
- `Task` : unité de travail d'un projet ;
- `TaskComment` : commentaire lié à une tâche ;
- `ProjectInvitation` : invitation à rejoindre un projet ;
- `Notification` : notification destinée à un utilisateur ;
- `ActivityLog` : trace d'une action importante.

### 25.2 Relations Principales

```text
User 1 ---- N Project
User 1 ---- N ProjectMember
Project 1 ---- N ProjectMember
Project 1 ---- N Task
Task 1 ---- N TaskComment
User 1 ---- N TaskComment
User 1 ---- N Task par assigned_to
Project 1 ---- N ProjectInvitation
User 1 ---- N Notification
Project 1 ---- N Notification
Project 1 ---- N ActivityLog
User 1 ---- N ActivityLog
```

### 25.3 Contraintes Importantes

- l'email utilisateur doit être unique ;
- le username utilisateur doit être unique ;
- le couple `(project_id, user_id)` doit être unique dans `project_members` ;
- le token d'invitation doit être unique ;
- une tâche appartient obligatoirement à un projet ;
- un commentaire appartient obligatoirement à une tâche et à un utilisateur ;
- un utilisateur assigné à une tâche doit être membre actif du projet.

## 26. Description de l'API REST

### 26.1 Endpoints Publics

```text
POST /auth/register
POST /auth/login
GET  /actuator/health
GET  /actuator/info
GET  /swagger-ui/**
GET  /v3/api-docs/**
```

### 26.2 Endpoints Protégés

Tous les autres endpoints nécessitent un token JWT valide.

### 26.3 Principaux Endpoints Métier

| Module | Endpoints |
| --- | --- |
| Authentification | `POST /auth/register`, `POST /auth/login`, `GET /auth/me` |
| Utilisateurs | `GET /users/{id}`, `GET /users/search` |
| Projets | `POST /projects`, `GET /projects`, `GET /projects/{id}`, `PUT /projects/{id}`, `DELETE /projects/{id}` |
| Statistiques | `GET /projects/{id}/statistics` |
| Tâches | `POST /projects/{projectId}/tasks`, `GET /projects/{projectId}/tasks`, `GET /tasks/{taskId}`, `PUT /tasks/{taskId}`, `DELETE /tasks/{taskId}` |
| Commentaires | `POST /tasks/{taskId}/comments`, `GET /tasks/{taskId}/comments`, `DELETE /tasks/{taskId}/comments/{commentId}` |
| Invitations | `POST /projects/{projectId}/invites`, `GET /projects/{projectId}/invites`, `GET /invites/{token}`, `POST /invites/{token}/accept`, `POST /invites/{token}/reject` |
| Membres | `GET /projects/{projectId}/members`, `PUT /projects/{projectId}/members/{memberId}` |
| Notifications | `GET /notifications`, `GET /notifications/unread`, `PATCH /notifications/{id}/read`, `PATCH /notifications/read-all` |
| Activités | `GET /projects/{projectId}/activity-logs` |

## 27. Modèle de Sécurité

### 27.1 Authentification JWT

Le système utilise une authentification stateless. Après connexion, le client reçoit un token JWT. Ce token doit être envoyé dans l'en-tête HTTP suivant :

```text
Authorization: Bearer <token>
```

Un filtre JWT valide le token à chaque requête protégée et place l'utilisateur authentifié dans le contexte de sécurité Spring.

### 27.2 Autorisation

Les accès sont contrôlés par :

- l'authentification de l'utilisateur ;
- l'appartenance active au projet ;
- le rôle du membre ;
- les permissions spécifiques du membre.

Un membre avec le statut `INVITED` ou `SUSPENDED` n'a pas accès aux ressources normales du projet.

### 27.3 Limitation du Débit

Le backend applique une limite globale de 100 requêtes par minute.

L'identifiant de limitation est :

- l'utilisateur authentifié si un JWT valide est présent ;
- l'adresse IP si l'utilisateur n'est pas authentifié.

En cas de dépassement, l'API retourne HTTP `429 TOO_MANY_REQUESTS`.

## 28. Règles Métier

- un utilisateur peut appartenir à plusieurs projets ;
- un projet possède exactement un propriétaire ;
- le propriétaire est automatiquement membre du projet ;
- un utilisateur ne peut pas être membre deux fois du même projet ;
- seuls les membres actifs peuvent accéder aux ressources d'un projet ;
- un membre suspendu ne peut pas accéder normalement au projet ;
- seuls les utilisateurs autorisés peuvent inviter des membres ;
- seuls les utilisateurs autorisés peuvent gérer les membres ;
- une tâche peut être assignée uniquement à un membre actif du projet ;
- une tâche peut rester non assignée ;
- le retard d'une tâche est indépendant de son statut ;
- une invitation expire après 7 jours ;
- seuls les auteurs peuvent supprimer leurs commentaires ;
- la suppression d'un projet supprime les données liées au projet mais pas les utilisateurs.

## 29. Stratégie de Test

### 29.1 Tests Automatisés

Le projet contient des tests pour les modules principaux :

- authentification ;
- projets ;
- tâches ;
- membres ;
- invitations ;
- notifications ;
- commentaires ;
- limitation de débit ;
- chargement du contexte Spring.

Les tests utilisent :

- JUnit 5 ;
- Mockito ;
- Spring Boot Test ;
- Spring Security Test ;
- H2 en mode compatibilité PostgreSQL.

### 29.2 Tests Manuels

Des tests manuels ont été réalisés avec des appels API/Postman :

- inscription ;
- connexion ;
- récupération du profil ;
- création de projet ;
- création et filtrage de tâches ;
- mise à jour et suppression de tâches ;
- création d'un second utilisateur ;
- création d'une invitation.

Un plan complet de tests Postman est fourni dans la section `13.1 Complete Postman Test Plan`. Il couvre :

- les endpoints publics de santé et documentation ;
- l'inscription, la connexion et la récupération du profil ;
- la gestion des utilisateurs et la recherche ;
- la création, consultation, modification, statistiques et suppression de projets ;
- la création, consultation, filtrage, modification, assignation et suppression de tâches ;
- la création, consultation et suppression de commentaires ;
- la création, consultation, acceptation et refus d'invitations ;
- l'envoi optionnel d'emails d'invitation ;
- la gestion des membres et permissions ;
- les notifications ;
- les journaux d'activité ;
- les cas d'erreur : accès non autorisé, conflit, validation invalide, ressource inexistante et rate limiting.

### 29.3 Résultat du Dernier Test

```text
mvn test

Tests run: 36
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

## 30. Diagrammes Proposés

### 30.1 Diagramme de Cas d'Utilisation

Acteurs :

- Visiteur ;
- Utilisateur authentifié ;
- Propriétaire de projet ;
- Membre de projet.

Cas d'utilisation :

- s'inscrire ;
- se connecter ;
- consulter son profil ;
- créer un projet ;
- consulter ses projets ;
- modifier ou supprimer un projet ;
- créer, modifier ou supprimer une tâche ;
- assigner une tâche ;
- commenter une tâche ;
- inviter un membre ;
- accepter ou refuser une invitation ;
- gérer les membres ;
- consulter les notifications ;
- marquer les notifications comme lues ;
- consulter les journaux d'activité.

### 30.2 Diagramme Entité-Association

Le diagramme doit représenter les entités `User`, `Project`, `ProjectMember`, `Task`, `TaskComment`, `ProjectInvitation`, `Notification` et `ActivityLog`.

Il doit montrer les relations principales :

- un utilisateur possède plusieurs projets ;
- un projet possède plusieurs membres ;
- un utilisateur appartient à plusieurs projets via `ProjectMember` ;
- un projet possède plusieurs tâches ;
- une tâche possède plusieurs commentaires ;
- un projet possède plusieurs invitations ;
- un utilisateur reçoit plusieurs notifications ;
- un projet possède plusieurs journaux d'activité.

### 30.3 Diagramme de Classes

Le diagramme de classes doit inclure :

- les entités métier ;
- les repositories ;
- les services ;
- les contrôleurs ;
- les DTO principaux.

Il doit mettre en évidence la séparation entre :

- couche API ;
- couche service ;
- couche persistance ;
- modèle métier.

### 30.4 Diagramme de Séquence - Connexion

Flux :

1. Le client envoie email et mot de passe à `/auth/login`.
2. `AuthController` appelle `AuthService`.
3. `AuthService` recherche l'utilisateur par email.
4. Le mot de passe est vérifié avec BCrypt.
5. `JwtService` génère un token JWT.
6. L'API retourne le token et les informations utilisateur.

### 30.5 Diagramme de Séquence - Création de Projet

Flux :

1. Le client envoie une requête `POST /projects` avec un JWT.
2. Le filtre JWT authentifie l'utilisateur.
3. `ProjectController` appelle `ProjectService`.
4. Le projet est enregistré.
5. Une entrée propriétaire est créée dans `project_members`.
6. Une activité est enregistrée.
7. L'API retourne le projet créé.

### 30.6 Diagramme de Séquence - Création de Tâche

Flux :

1. Le client envoie une requête `POST /projects/{projectId}/tasks`.
2. Le backend vérifie l'authentification JWT.
3. Le backend vérifie que l'utilisateur est membre actif du projet.
4. Le backend vérifie la permission de création de tâche.
5. `TaskService` crée la tâche.
6. Si un assigné est fourni, le backend vérifie qu'il est membre actif du projet.
7. La tâche est enregistrée.
8. Une activité est ajoutée.
9. L'API retourne la tâche créée.

### 30.7 Diagramme de Séquence - Création d'Invitation

Flux :

1. Le propriétaire ou un membre autorisé envoie une invitation.
2. Le backend vérifie les droits d'invitation.
3. Un token unique est généré.
4. L'invitation est enregistrée avec le statut `PENDING`.
5. Une notification est créée si l'email correspond à un utilisateur existant.
6. Une activité est enregistrée.
7. L'API retourne les détails de l'invitation.

### 30.8 Diagramme de Séquence - Acceptation d'Invitation

Flux :

1. L'utilisateur invité envoie une requête `POST /invites/{token}/accept`.
2. Le backend vérifie le token.
3. Le backend vérifie le statut et la date d'expiration.
4. Le backend vérifie que l'utilisateur correspond à l'email invité si nécessaire.
5. Une entrée `ProjectMember` est créée avec le rôle et les permissions proposés.
6. L'invitation passe au statut `ACCEPTED`.
7. Une notification est envoyée à l'invitant.
8. L'API retourne le résultat de l'acceptation.

## 31. Contraintes Techniques

- Le backend doit rester compatible avec Java 21.
- Les migrations Flyway doivent rester la source de vérité du schéma.
- Les endpoints doivent conserver une structure REST cohérente.
- Les exceptions doivent être traduites en réponses HTTP claires.
- Les règles d'accès ne doivent pas être déléguées uniquement au frontend.
- Les tests doivent pouvoir être exécutés localement avec `mvn test`.

## 31.1 Améliorations Fonctionnelles Recommandées pour l'Expérience Utilisateur

Pour améliorer l'expérience utilisateur, les fonctionnalités suivantes sont recommandées pour les prochaines itérations.

### Envoi d'Emails d'Invitation

L'application peut envoyer automatiquement un email lorsqu'un utilisateur est invité à rejoindre un projet. L'email contient un lien vers l'interface frontend permettant d'accepter ou de refuser l'invitation.

Approche recommandée :

- utiliser `spring-boot-starter-mail` ;
- configurer un fournisseur SMTP transactionnel ;
- activer ou désactiver l'envoi avec `MAIL_ENABLED` ;
- utiliser `APP_FRONTEND_URL` pour générer les liens d'invitation ;
- conserver la création d'invitation même si l'envoi email est désactivé en environnement local.
- journaliser les erreurs SMTP sans annuler la création de l'invitation.

Fournisseur recommandé :

```text
Resend via SMTP
```

Resend est recommandé pour ce projet car il est simple à configurer, adapté aux emails transactionnels et compatible avec une intégration SMTP standard Spring Boot. SendGrid et Mailgun restent de bonnes alternatives pour des besoins plus avancés.

### Rappels d'Échéance

Le système peut envoyer des notifications lorsqu'une tâche :

- arrive à échéance aujourd'hui ;
- arrive à échéance demain ;
- est en retard.

Ces rappels peuvent être générés par une tâche planifiée quotidienne.

### Étiquettes de Tâches

Les tâches peuvent être classées avec des étiquettes telles que :

- Backend ;
- Frontend ;
- Bug ;
- Urgent ;
- Design ;
- Testing.

Cela facilite la recherche, le filtrage et l'organisation du travail.

### Historique Détaillé d'une Tâche

Chaque tâche peut avoir une chronologie détaillée indiquant :

- la création de la tâche ;
- les changements de statut ;
- les changements de priorité ;
- les changements d'assignation ;
- l'ajout ou la modification de commentaires.

Cette fonctionnalité améliore la traçabilité au niveau opérationnel.

### Tableau de Bord Amélioré

Le tableau de bord projet peut être enrichi avec :

- répartition des tâches par priorité ;
- répartition des tâches par membre assigné ;
- liste des tâches en retard ;
- activité récente ;
- évolution du pourcentage d'avancement.

### Mise à Jour du Profil Utilisateur

L'utilisateur doit pouvoir modifier :

- son nom ;
- son nom d'utilisateur ;
- son mot de passe ;
- l'URL de son image de profil.

### Archivage de Projet

L'archivage permet de masquer un projet terminé sans le supprimer définitivement. Cette solution est plus sûre que la suppression directe pour les projets terminés ou suspendus.

### Pièces Jointes ou Liens sur les Tâches

Les tâches peuvent accepter des fichiers ou des liens externes pour stocker :

- captures d'écran ;
- documents de spécification ;
- maquettes ;
- liens vers des ressources externes.

### Préférences de Notifications

Chaque utilisateur peut choisir les événements qui génèrent des notifications :

- invitations ;
- assignations de tâches ;
- commentaires ;
- rappels d'échéance ;
- changements de statut.

## 32. Conclusion

Le backend GestProj fournit une base complète pour une application collaborative de gestion de projets. Il couvre les besoins essentiels d'authentification, de gestion de projets, de gestion des membres, de suivi des tâches, de collaboration par commentaires, de notifications et de traçabilité.

Son architecture par fonctionnalité, son modèle de sécurité JWT, ses migrations Flyway et sa couverture de tests en font une base adaptée à une évolution progressive vers une application complète avec frontend, déploiement Docker, intégration continue et fonctionnalités avancées.
