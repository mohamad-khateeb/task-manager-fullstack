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

- Java 17 or higher
- Maven 3.6+
- AWS Cognito User Pool (for authentication)
- Docker (optional, for containerized deployment)

## Configuration

### AWS Cognito Setup

1. **Create a Cognito User Pool** in AWS Console
2. **Create User Groups**:
   - `ADMIN` - for admin users
   - `USER` - for regular users
3. **Get your JWKS URI**:
   - Format: `https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json`
   - Example: `https://cognito-idp.us-east-1.amazonaws.com/us-east-1_ABC123XYZ/.well-known/jwks.json`

### application.yml Configuration

Update the `application.yml` file with your Cognito configuration:

```yaml
security:
  oauth2:
    resourceserver:
      jwt:
        jwk-set-uri: https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json
```

Replace `{region}` and `{userPoolId}` with your actual AWS Cognito values.

### Cognito Groups to Roles Mapping

The application automatically maps Cognito groups to Spring Security roles:
- Cognito group `ADMIN` → Spring role `ROLE_ADMIN`
- Cognito group `USER` → Spring role `ROLE_USER`

Make sure your Cognito ID tokens include the `cognito:groups` claim.

## API Endpoints

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

All API endpoints under `/api/**` require authentication. You must include a valid AWS Cognito ID token in the Authorization header:

```
Authorization: Bearer <your-cognito-id-token>
```

### Getting a Cognito ID Token

1. Use AWS Cognito Hosted UI or
2. Use AWS Cognito SDK to authenticate and get the ID token
3. Include the token in the `Authorization` header of your requests

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
mvn test
```

### Test Coverage

The project includes unit tests for:
- `ProjectServiceTest` - Service layer tests for projects
- `TaskServiceTest` - Service layer tests for tasks
- `ProjectControllerTest` - Controller layer tests for projects

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

### Docker Compose (Optional)

You can create a `docker-compose.yml` for easier deployment:

```yaml
version: '3.8'
services:
  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json
```

## Example Requests

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

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### H2 Console

For development, the H2 console is available at:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:taskdb`
- Username: `sa`
- Password: (empty)

## Project Structure

```
com.example.taskmanager
├── TaskManagerApplication.java
├── config/
│   └── SecurityConfig.java
├── controller/
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
    ├── ProjectService.java
    └── TaskService.java
```

## License

This project is part of a home assignment.

