# Task Manager - Full Stack Application

A full-stack task management application with AWS Cognito authentication.

## Tech Stack

### Backend
- Java 21
- Spring Boot 3.2.0
- Spring Security with OAuth2 Resource Server
- AWS Cognito for authentication
- PostgreSQL Database
- Maven

### Frontend
- React 19.2.0
- React Router 7.9.4
- Axios for API calls
- Context API for state management

## Features

- ✅ User authentication with AWS Cognito
- ✅ Role-based access control (ADMIN, USER)
- ✅ Project management (CRUD operations)
- ✅ Task management (CRUD operations)
- ✅ Pagination support
- ✅ Responsive UI
- ✅ Comprehensive unit tests (44 tests)
- ✅ Error handling and logging
- ✅ Production-ready deployment guide

## Prerequisites

- Java 21
- Maven 3.6+
- Node.js 16+
- Docker (for PostgreSQL) or PostgreSQL installed locally
- AWS Cognito User Pool (see setup guides)

## Setup

### Database Setup

Start PostgreSQL using Docker:
```bash
docker-compose up -d
```

This will start PostgreSQL on port 5432 with:
- Database: `taskdb`
- Username: `postgres`
- Password: `postgres`

### Backend Setup

1. Copy `backend/src/main/resources/application.yml.template` to `application.yml`
2. Update `application.yml` with your AWS Cognito configuration
3. Ensure PostgreSQL is running (via Docker or locally)
4. Run: `cd backend && mvn spring-boot:run`

The database tables will be created automatically on first run.

### Frontend Setup

1. Install dependencies: `cd frontend && npm install`
2. Start development server: `npm start`

## Testing

### Backend Tests

Run all unit tests:
```bash
cd backend
mvn test
```

The test suite includes:
- **44 unit tests** covering all core functionality
- Service layer tests (ProjectService, TaskService, AuthService)
- Controller layer tests (ProjectController, TaskController, AuthController)
- Exception handler tests
- Test coverage: Core business logic and API endpoints

### Test Results
```
Tests run: 44, Failures: 0, Errors: 0, Skipped: 0
```

## Configuration

See the following guides for detailed setup:
- `COGNITO_SETUP_GUIDE.md` - AWS Cognito setup
- `COGNITO_QUICK_START.md` - Quick reference
- `HOW_IT_WORKS.md` - Technical details
- `DEPLOYMENT.md` - **Production deployment guide for 10k+ users/day**

## API Endpoints

- `POST /api/auth/login` - User authentication
- `GET /api/projects` - Get all projects (paginated)
- `POST /api/projects` - Create project
- `PUT /api/projects/{id}` - Update project
- `DELETE /api/projects/{id}` - Delete project (ADMIN only)
- `GET /api/projects/{projectId}/tasks` - Get tasks for a project
- `POST /api/projects/{projectId}/tasks` - Create task
- `PUT /api/projects/{projectId}/tasks/{taskId}` - Update task
- `DELETE /api/projects/{projectId}/tasks/{taskId}` - Delete task (ADMIN only)

## Project Structure

```
task-manager/
├── backend/          # Spring Boot backend
│   ├── src/
│   └── pom.xml
├── frontend/         # React frontend
│   ├── src/
│   └── package.json
└── README.md
```

## License

This project is part of a home assignment.


## Photos

 - Logib page : 

<img width="860" height="630" alt="image" src="https://github.com/user-attachments/assets/2688d145-1f89-46c2-8312-40029a7472d9" />

- Projects page :

<img width="1299" height="742" alt="image" src="https://github.com/user-attachments/assets/766b858a-2e34-424d-b11e-395e0bf101f2" />


- Tasks page :
    
  <img width="1299" height="742" alt="image" src="https://github.com/user-attachments/assets/ec798b5e-424b-47eb-922e-d99751d30671" />

