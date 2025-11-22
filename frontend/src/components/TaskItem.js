import React from 'react';
import './TaskItem.css';

const TaskItem = ({ task, onEdit, onDelete }) => {
  const getStatusColor = (status) => {
    switch (status) {
      case 'TODO':
        return 'status-todo';
      case 'IN_PROGRESS':
        return 'status-in-progress';
      case 'DONE':
        return 'status-done';
      default:
        return 'status-todo';
    }
  };

  return (
    <div className="task-item">
      <div className="task-header">
        <div className="task-title-section">
          <h3>{task.title}</h3>
          <span className={`status-badge ${getStatusColor(task.status)}`}>
            {task.status}
          </span>
        </div>
        <div className="task-actions">
          <button onClick={() => onEdit(task)} className="btn-icon" title="Edit">
            ✏️
          </button>
          <button onClick={() => onDelete(task.id)} className="btn-icon" title="Delete">
            🗑️
          </button>
        </div>
      </div>
      {task.description && (
        <p className="task-description">{task.description}</p>
      )}
    </div>
  );
};

export default TaskItem;

