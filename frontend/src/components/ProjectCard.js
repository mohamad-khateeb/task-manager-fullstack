import React from 'react';
import { useNavigate } from 'react-router-dom';
import './ProjectCard.css';

const ProjectCard = ({ project, onEdit, onDelete }) => {
  const navigate = useNavigate();

  const handleViewTasks = () => {
    navigate(`/projects/${project.id}/tasks`);
  };

  return (
    <div className="project-card">
      <div className="project-card-header">
        <h3>{project.name}</h3>
        <div className="project-actions">
          <button onClick={() => onEdit(project)} className="btn-icon" title="Edit">
            ✏️
          </button>
          <button onClick={() => onDelete(project.id)} className="btn-icon" title="Delete">
            🗑️
          </button>
        </div>
      </div>
      {project.description && (
        <p className="project-description">{project.description}</p>
      )}
      <button onClick={handleViewTasks} className="btn-view-tasks">
        View Tasks →
      </button>
    </div>
  );
};

export default ProjectCard;

