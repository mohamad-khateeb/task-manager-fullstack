import axios from 'axios';

// Create a preconfigured Axios instance
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - adds JWT token from localStorage
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('cognito_id_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - handles common errors
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // Clear token and redirect to login
      localStorage.removeItem('cognito_id_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Projects API
export const projectsApi = {
  getAll: (page = 0, size = 10, sort = 'id,desc') => {
    return api.get('/projects', { params: { page, size, sort } });
  },
  getById: (id) => {
    return api.get(`/projects/${id}`);
  },
  create: (project) => {
    return api.post('/projects', project);
  },
  update: (id, project) => {
    return api.put(`/projects/${id}`, project);
  },
  delete: (id) => {
    return api.delete(`/projects/${id}`);
  },
};

// Tasks API
export const tasksApi = {
  getAll: (projectId, page = 0, size = 10, sort = 'id,desc') => {
    return api.get(`/projects/${projectId}/tasks`, { params: { page, size, sort } });
  },
  getById: (projectId, taskId) => {
    return api.get(`/projects/${projectId}/tasks/${taskId}`);
  },
  create: (projectId, task) => {
    return api.post(`/projects/${projectId}/tasks`, task);
  },
  update: (projectId, taskId, task) => {
    return api.put(`/projects/${projectId}/tasks/${taskId}`, task);
  },
  delete: (projectId, taskId) => {
    return api.delete(`/projects/${projectId}/tasks/${taskId}`);
  },
};

export default api;
