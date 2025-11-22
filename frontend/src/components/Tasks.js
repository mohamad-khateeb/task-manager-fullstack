import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { tasksApi, projectsApi } from '../services/api';
import TaskForm from './TaskForm';
import TaskItem from './TaskItem';
import './Tasks.css';

const Tasks = () => {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const [tasks, setTasks] = useState([]);
  const [project, setProject] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [editingTask, setEditingTask] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 10;

  useEffect(() => {
    loadProject();
    loadTasks();
  }, [projectId, page]);

  const loadProject = async () => {
    try {
      const response = await projectsApi.getById(projectId);
      setProject(response.data);
    } catch (err) {
      setError('Failed to load project');
    }
  };

  const loadTasks = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await tasksApi.getAll(projectId, page, pageSize, 'id,desc');
      setTasks(response.data.content);
      setTotalPages(response.data.totalPages);
      setTotalElements(response.data.totalElements);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load tasks');
      console.error('Error loading tasks:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingTask(null);
    setShowForm(true);
  };

  const handleEdit = (task) => {
    setEditingTask(task);
    setShowForm(true);
  };

  const handleDelete = async (taskId) => {
    if (window.confirm('Are you sure you want to delete this task?')) {
      try {
        await tasksApi.delete(projectId, taskId);
        loadTasks();
      } catch (err) {
        alert(err.response?.data?.message || 'Failed to delete task');
      }
    }
  };

  const handleFormClose = () => {
    setShowForm(false);
    setEditingTask(null);
    loadTasks();
  };

  if (loading && tasks.length === 0) {
    return <div className="loading">Loading tasks...</div>;
  }

  return (
    <div className="tasks-container">
      <div className="tasks-header">
        <button onClick={() => navigate('/projects')} className="btn-back">
          ← Back to Projects
        </button>
        <div className="tasks-header-content">
          <h1>{project?.name || 'Tasks'}</h1>
          <button onClick={handleCreate} className="btn-primary">+ New Task</button>
        </div>
        {project?.description && (
          <p className="project-description">{project.description}</p>
        )}
      </div>

      {error && <div className="error-message">{error}</div>}

      {showForm && (
        <TaskForm
          projectId={projectId}
          task={editingTask}
          onClose={handleFormClose}
        />
      )}

      {tasks.length === 0 ? (
        <div className="empty-state">
          <p>No tasks yet. Create your first task!</p>
        </div>
      ) : (
        <>
          <div className="tasks-list">
            {tasks.map((task) => (
              <TaskItem
                key={task.id}
                task={task}
                onEdit={handleEdit}
                onDelete={handleDelete}
              />
            ))}
          </div>

          <div className="pagination">
            <button
              onClick={() => setPage(0)}
              disabled={page === 0}
              className="btn-secondary"
            >
              First
            </button>
            <button
              onClick={() => setPage(page - 1)}
              disabled={page === 0}
              className="btn-secondary"
            >
              Previous
            </button>
            <span className="page-info">
              Page {page + 1} of {totalPages} ({totalElements} total)
            </span>
            <button
              onClick={() => setPage(page + 1)}
              disabled={page >= totalPages - 1}
              className="btn-secondary"
            >
              Next
            </button>
            <button
              onClick={() => setPage(totalPages - 1)}
              disabled={page >= totalPages - 1}
              className="btn-secondary"
            >
              Last
            </button>
          </div>
        </>
      )}
    </div>
  );
};

export default Tasks;

