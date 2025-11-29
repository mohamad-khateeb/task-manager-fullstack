# Task Manager Frontend

A modern React application for managing projects and tasks with AWS Cognito authentication.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Setup](#setup)
- [Project Structure](#project-structure)
- [Authentication](#authentication)
- [API Integration](#api-integration)
- [Components](#components)
- [Available Scripts](#available-scripts)
- [Configuration](#configuration)

## Features

- **User Authentication**: Email/password login with AWS Cognito via backend API
- **Project Management**: Full CRUD operations for projects with pagination
- **Task Management**: Full CRUD operations for tasks within projects
- **Role-Based Access**: UI adapts based on user role (ADMIN/USER)
- **Pagination**: Efficient data loading with pagination support
- **Responsive Design**: Modern, mobile-friendly UI
- **Protected Routes**: Automatic redirect to login for unauthenticated users
- **Error Handling**: Comprehensive error handling with user-friendly messages

## Tech Stack

- **React 19.2.0** - UI library
- **React Router 7.9.4** - Client-side routing
- **Axios 1.12.2** - HTTP client for API calls
- **Context API** - State management for authentication
- **CSS Modules** - Component-scoped styling

## Prerequisites

- Node.js 16+ and npm
- Backend API running on `http://localhost:8080`
- AWS Cognito User Pool configured (see backend documentation)

## Setup

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Start Development Server

```bash
npm start
```

The application will open in your browser at `http://localhost:3000`.

### 3. Backend Configuration

Ensure the backend API is running and accessible at `http://localhost:8080`. The frontend is configured to communicate with the backend API at this address.

To change the backend URL, update the `baseURL` in `src/services/api.js`:

```javascript
const api = axios.create({
  baseURL: 'http://localhost:8080/api', // Change this if needed
  // ...
});
```

## Project Structure

```
frontend/
├── public/
│   ├── index.html
│   └── ...
├── src/
│   ├── components/          # React components
│   │   ├── Login.js         # Login page
│   │   ├── Projects.js      # Projects list and management
│   │   ├── Tasks.js         # Tasks list and management
│   │   ├── ProjectForm.js   # Create/Edit project form
│   │   ├── TaskForm.js      # Create/Edit task form
│   │   ├── ProjectCard.js   # Project card component
│   │   ├── TaskItem.js      # Task item component
│   │   ├── Navbar.js        # Navigation bar
│   │   └── *.css            # Component styles
│   ├── contexts/
│   │   └── AuthContext.js   # Authentication context provider
│   ├── services/
│   │   ├── api.js           # Axios instance and API methods
│   │   └── cognitoAuth.js   # Authentication service
│   ├── App.js               # Main app component with routing
│   ├── App.css              # Global app styles
│   ├── index.js             # Application entry point
│   └── index.css            # Global styles
└── package.json
```

## Authentication

### Login Flow

1. User enters email and password on the login page
2. Frontend sends credentials to backend `/api/auth/login` endpoint
3. Backend authenticates with AWS Cognito and returns ID token
4. Frontend stores token in `localStorage` as `cognito_id_token`
5. Token is automatically included in all subsequent API requests
6. User is redirected to the projects page

### Token Management

- **Storage**: ID token is stored in `localStorage` under key `cognito_id_token`
- **Automatic Injection**: Axios interceptor automatically adds token to all API requests
- **Expiration Handling**: On 401 responses, token is cleared and user is redirected to login
- **Logout**: Clears token from storage and updates authentication state

### Protected Routes

Routes are protected using a `PrivateRoute` component that checks authentication status:

```javascript
<PrivateRoute>
  <Projects />
</PrivateRoute>
```

Unauthenticated users are automatically redirected to `/login`.

## API Integration

### Axios Configuration

The application uses a preconfigured Axios instance (`src/services/api.js`) that:

- Sets base URL to `http://localhost:8080/api`
- Automatically adds JWT token to all requests via request interceptor
- Handles 401 responses by clearing token and redirecting to login
- Provides timeout and error handling

### API Methods

**Projects API** (`projectsApi`):
- `getAll(page, size, sort)` - Get paginated projects
- `getById(id)` - Get project by ID
- `create(project)` - Create new project
- `update(id, project)` - Update project
- `delete(id)` - Delete project

**Tasks API** (`tasksApi`):
- `getAll(projectId, page, size, sort)` - Get paginated tasks for a project
- `getById(projectId, taskId)` - Get task by ID
- `create(projectId, task)` - Create new task
- `update(projectId, taskId, task)` - Update task
- `delete(projectId, taskId)` - Delete task

### Error Handling

The application handles various error scenarios:

- **Network Errors**: Displays connection error messages
- **Authentication Errors**: Redirects to login with appropriate messages
- **Validation Errors**: Shows field-specific error messages
- **Server Errors**: Displays user-friendly error messages

## Components

### Login Component

- Email and password input fields
- Loading state during authentication
- Error message display
- Automatic redirect on successful login

### Projects Component

- Displays paginated list of projects
- Create new project button (ADMIN/USER)
- Edit and delete buttons (role-based)
- Pagination controls
- Project cards with name and description

### Tasks Component

- Displays tasks for a specific project
- Create new task button (ADMIN/USER)
- Edit and delete buttons (role-based)
- Task status indicators (TODO, IN_PROGRESS, DONE)
- Pagination controls
- Filter and sort options

### ProjectForm Component

- Modal form for creating/editing projects
- Name and description fields
- Validation
- Submit and cancel buttons

### TaskForm Component

- Modal form for creating/editing tasks
- Title, description, and status fields
- Status dropdown (TODO, IN_PROGRESS, DONE)
- Validation
- Submit and cancel buttons

### Navbar Component

- Application title/logo
- User authentication status
- Logout button (when authenticated)
- Navigation links

## Available Scripts

### `npm start`

Runs the app in development mode at `http://localhost:3000`.

The page will reload when you make changes. You may also see lint errors in the console.

### `npm test`

Launches the test runner in interactive watch mode.

### `npm run build`

Builds the app for production to the `build` folder.

The build is minified and optimized for best performance. The app is ready to be deployed.

### `npm run eject`

**Note: This is a one-way operation. Once you `eject`, you can't go back!**

If you aren't satisfied with the build tool and configuration choices, you can `eject` at any time. This command will remove the single build dependency from your project.

## Configuration

### Environment Variables

Currently, the backend URL is hardcoded in `src/services/api.js`. For production deployment, consider using environment variables:

1. Create a `.env` file in the `frontend` directory:
   ```
   REACT_APP_API_URL=http://localhost:8080/api
   ```

2. Update `src/services/api.js`:
   ```javascript
   const api = axios.create({
     baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
     // ...
   });
   ```

### CORS Configuration

Ensure the backend has CORS configured to allow requests from `http://localhost:3000` during development. For production, update CORS settings to match your frontend domain.

## Development Notes

### Backend Dependency

The frontend requires the backend API to be running. Ensure:

1. Backend is running on `http://localhost:8080`
2. PostgreSQL database is running (via Docker Compose)
3. AWS Cognito is properly configured
4. CORS is enabled for `http://localhost:3000`

### Authentication Flow

The authentication flow is handled entirely through the backend:

1. Frontend sends email/password to backend `/api/auth/login`
2. Backend authenticates with AWS Cognito
3. Backend returns ID token to frontend
4. Frontend stores token and uses it for all API requests

This approach simplifies the frontend and centralizes authentication logic in the backend.

### State Management

The application uses React Context API for authentication state management:

- `AuthContext` provides authentication state and methods
- `useAuth` hook provides easy access to auth context
- Token persistence via `localStorage`

## Troubleshooting

### Backend Connection Issues

If you see network errors:

1. Verify backend is running: `curl http://localhost:8080/api/auth/diagnostic`
2. Check backend logs for errors
3. Verify CORS configuration in backend

### Authentication Issues

If login fails:

1. Verify Cognito configuration in backend
2. Check user exists in Cognito User Pool
3. Verify user password is correct
4. Check browser console for detailed error messages

### Build Issues

If `npm run build` fails:

1. Clear `node_modules` and reinstall: `rm -rf node_modules && npm install`
2. Check Node.js version (requires 16+)
3. Review build error messages for specific issues

## License

This project is part of a home assignment.
