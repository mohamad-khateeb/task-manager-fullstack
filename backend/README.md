# Task Manager Backend API

A REST API for a Task Management System built with Spring Boot, featuring AWS Cognito JWT authentication, project and task management with pagination support.

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Authentication](#authentication)
- [Pagination](#pagination)
- [Testing](#testing)
- [Docker](#docker)
- [Example Requests](#example-requests)

## Features

- **Authentication**: AWS Cognito JWT validation with role-based access control
- **Projects**: Full CRUD operations for project management
- **Tasks**: Full CRUD operations for task management (nested under projects)
- **Pagination**: Spring Pageable support on all GET endpoints
- **Error Handling**: Comprehensive exception handling with proper HTTP status codes
- **Logging**: SLF4J logging throughout services and exception handlers
- **Security**: Method-level security with role-based authorization

## Prerequisites

- Java 21
- Maven 3.6+
- PostgreSQL 15+ (or Docker for running PostgreSQL)
- AWS Cognito User Pool (for authentication)
- Docker (for PostgreSQL database setup)

## Configuration

### Database Setup

The application uses PostgreSQL as the persistent database. The easiest way to run PostgreSQL locally is using Docker Compose:

```bash
# From the project root directory
docker-compose up -d
```

This will start PostgreSQL on port 5432 with:
- Database: `taskdb`
- Username: `postgres`
- Password: `postgres`

The database tables will be created automatically on first application startup using Hibernate's `ddl-auto: update`.

Alternatively, you can use a local PostgreSQL installation. Update the connection details in `application.yml` accordingly.

### AWS Cognito Setup

1. **Create a Cognito User Pool** in AWS Console
2. **Create User Groups**:
   - `ADMIN` - for admin users
   - `USER` - for regular users
3. **Create an App Client** (without client secret, for web applications)
4. **Enable Authentication Flows**: `ALLOW_USER_PASSWORD_AUTH` and `ALLOW_REFRESH_TOKEN_AUTH`
5. **Get your configuration values**:
   - User Pool ID
   - Region
   - App Client ID
   - JWKS URI: `https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json`

### application.yml Configuration

1. **Copy the template file**:
   ```bash
   cp src/main/resources/application.yml.template src/main/resources/application.yml
   ```

2. **Update `application.yml`** with your configuration:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/taskdb
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

security:
  oauth2:
    resourceserver:
      jwt:
        jwk-set-uri: https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json

cognito:
  userPoolId: YOUR_USER_POOL_ID
  region: YOUR_REGION
  appClientId: YOUR_APP_CLIENT_ID
```

Replace the placeholder values with your actual AWS Cognito configuration.

### Cognito Groups to Roles Mapping

The application automatically maps Cognito groups to Spring Security roles:
- Cognito group `ADMIN` → Spring role `ROLE_ADMIN`
- Cognito group `USER` → Spring role `ROLE_USER`

Make sure your Cognito ID tokens include the `cognito:groups` claim.

## API Endpoints

### Authentication

| Method | Endpoint | Description | Authentication Required |
|--------|----------|-------------|------------------------|
| POST | `/api/auth/login` | Authenticate user with email/password | No (public endpoint) |
| GET | `/api/auth/diagnostic` | Health check endpoint | No (public endpoint) |

### Projects

| Method | Endpoint | Description | Roles Required |
|--------|----------|-------------|----------------|
| GET | `/api/projects` | Get paginated list of projects | Any authenticated user |
| GET | `/api/projects/{id}` | Get project by ID | Any authenticated user |
| POST | `/api/projects` | Create a new project | ADMIN, USER |
| PUT | `/api/projects/{id}` | Update a project | ADMIN, USER |
| DELETE | `/api/projects/{id}` | Delete a project | ADMIN only |

### Tasks

| Method | Endpoint | Description | Roles Required |
|--------|----------|-------------|----------------|
| GET | `/api/projects/{projectId}/tasks` | Get paginated list of tasks for a project | Any authenticated user |
| GET | `/api/projects/{projectId}/tasks/{taskId}` | Get task by ID | Any authenticated user |
| POST | `/api/projects/{projectId}/tasks` | Create a new task | ADMIN, USER |
| PUT | `/api/projects/{projectId}/tasks/{taskId}` | Update a task | ADMIN, USER |
| DELETE | `/api/projects/{projectId}/tasks/{taskId}` | Delete a task | ADMIN only |

## Authentication

All API endpoints under `/api/**` (except `/api/auth/login` and `/api/auth/diagnostic`) require authentication. You must include a valid AWS Cognito ID token in the Authorization header:

```
Authorization: Bearer <your-cognito-id-token>
```

### Login Endpoint

The application provides a login endpoint that handles authentication with AWS Cognito:

**POST `/api/auth/login`**

Request body:
```json
{
  "email": "user@example.com",
  "password": "userpassword"
}
```

Response (success):
```json
{
  "idToken": "eyJraWQiOiJ...",
  "accessToken": "eyJraWQiOiJ...",
  "refreshToken": "eyJjdHkiOiJ...",
  "expiresIn": 3600
}
```

The `idToken` should be used in subsequent API requests in the `Authorization` header.

### Getting a Cognito ID Token

There are several ways to obtain a Cognito ID token:

1. **Use the login endpoint** (recommended): `POST /api/auth/login` with email/password
2. **Use AWS Cognito Hosted UI**: Redirect users to Cognito's hosted UI
3. **Use AWS Cognito SDK**: Authenticate programmatically using AWS SDK
4. **Manual token retrieval**: Use AWS CLI (see helper scripts in project root)

Once you have the ID token, include it in the `Authorization` header of your requests.

## Pagination

All GET endpoints support pagination using Spring's `Pageable` interface.

### Pagination Parameters

- `page`: Page number (0-indexed, default: 0)
- `size`: Number of items per page (default: 20)
- `sort`: Sort field and direction (e.g., `name,asc` or `id,desc`)

### Pagination Examples

```
GET /api/projects?page=0&size=10
GET /api/projects?page=1&size=20&sort=name,asc
GET /api/projects/1/tasks?page=0&size=5&sort=status,desc
```

### Pagination Response Format

```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 25,
  "totalPages": 3,
  "number": 0,
  "size": 10,
  "first": true,
  "last": false
}
```

## Testing

### Running Tests

```bash
cd backend
mvn test
```

### Test Coverage

The project includes **44 comprehensive unit tests** covering all core functionality:

**Service Layer Tests:**
- `ProjectServiceTest` - Project CRUD operations, pagination, validation
- `TaskServiceTest` - Task CRUD operations, project association, status management
- `AuthServiceTest` - Authentication flow, error handling, temporary password detection

**Controller Layer Tests:**
- `ProjectControllerTest` - Project endpoints, pagination, role-based access
- `TaskControllerTest` - Task endpoints, pagination, validation
- `AuthControllerTest` - Login endpoint, error responses, diagnostic endpoint

**Exception Handler Tests:**
- `ApiExceptionHandlerTest` - Global exception handling, error response formatting

### Test Results

All tests should pass with:
```
Tests run: 44, Failures: 0, Errors: 0, Skipped: 0
```

The test suite validates:
- CRUD operations for projects and tasks
- Pagination functionality
- Role-based access control (ADMIN vs USER)
- Authentication and authorization
- Error handling and validation
- Exception responses

## Docker

### Building the Docker Image

```bash
# First, build the JAR file
mvn clean package

# Build the Docker image
docker build -t task-manager-backend .
```

### Running with Docker

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json \
  task-manager-backend
```

### Docker Compose for Database

The project includes a `docker-compose.yml` file in the root directory for running PostgreSQL:

```bash
# From project root
docker-compose up -d
```

This starts PostgreSQL in a container with persistent data storage.

## Example Requests

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "userpassword"
  }'
```

Response:
```json
{
  "idToken": "eyJraWQiOiJ...",
  "accessToken": "eyJraWQiOiJ...",
  "refreshToken": "eyJjdHkiOiJ...",
  "expiresIn": 3600
}
```

Save the `idToken` for subsequent requests.

### Create a Project

```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Project",
    "description": "Project description"
  }'
```

### Get All Projects (Paginated)

```bash
curl -X GET "http://localhost:8080/api/projects?page=0&size=10" \
  -H "Authorization: Bearer <your-token>"
```

### Get Project by ID

```bash
curl -X GET http://localhost:8080/api/projects/1 \
  -H "Authorization: Bearer <your-token>"
```

### Update a Project

```bash
curl -X PUT http://localhost:8080/api/projects/1 \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Project Name",
    "description": "Updated description"
  }'
```

### Delete a Project (Admin only)

```bash
curl -X DELETE http://localhost:8080/api/projects/1 \
  -H "Authorization: Bearer <your-admin-token>"
```

### Create a Task

```bash
curl -X POST http://localhost:8080/api/projects/1/tasks \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete task",
    "description": "Task description",
    "status": "TODO"
  }'
```

### Get Tasks for a Project (Paginated)

```bash
curl -X GET "http://localhost:8080/api/projects/1/tasks?page=0&size=10&sort=status,asc" \
  -H "Authorization: Bearer <your-token>"
```

### Update a Task

```bash
curl -X PUT http://localhost:8080/api/projects/1/tasks/1 \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Task",
    "description": "Updated description",
    "status": "IN_PROGRESS"
  }'
```

### Delete a Task (Admin only)

```bash
curl -X DELETE http://localhost:8080/api/projects/1/tasks/1 \
  -H "Authorization: Bearer <your-admin-token>"
```

## Task Status Values

Tasks can have one of the following statuses:
- `TODO` - Task is not started
- `IN_PROGRESS` - Task is in progress
- `DONE` - Task is completed

## Error Responses

The API returns standardized error responses:

```json
{
  "status": 404,
  "message": "Project not found with id: 1",
  "timestamp": "2024-01-15T10:30:00"
}
```

Common HTTP status codes:
- `200 OK` - Successful GET, PUT requests
- `201 Created` - Successful POST requests
- `204 No Content` - Successful DELETE requests
- `400 Bad Request` - Validation errors
- `401 Unauthorized` - Missing or invalid JWT token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server errors

## Local Development

### Running the Application

1. **Start PostgreSQL** (if not already running):
   ```bash
   # From project root
   docker-compose up -d
   ```

2. **Configure the application**:
   - Copy `src/main/resources/application.yml.template` to `src/main/resources/application.yml`
   - Update with your Cognito configuration

3. **Run the application**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

The database tables will be created automatically on first startup.

## Project Structure

```
com.example.taskmanager
├── TaskManagerApplication.java
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   ├── ProjectController.java
│   └── TaskController.java
├── dto/
│   ├── ProjectDto.java
│   └── TaskDto.java
├── entity/
│   ├── Project.java
│   └── Task.java
├── exception/
│   ├── ApiExceptionHandler.java
│   └── ResourceNotFoundException.java
├── repository/
│   ├── ProjectRepository.java
│   └── TaskRepository.java
└── service/
    ├── AuthService.java
    ├── ProjectService.java
    └── TaskService.java
```

## License

This project is part of a home assignment.

