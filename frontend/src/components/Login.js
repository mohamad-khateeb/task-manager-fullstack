import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import { authenticateUser } from '../services/cognitoAuth';
import './Login.css';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // Authenticate with Cognito
      const idToken = await authenticateUser(email, password);
      
      // Save token and update auth state
      login(idToken);
      
      // Redirect to projects page
      navigate('/projects');
    } catch (err) {
      console.error('Login error:', err);
      
      // Handle different error types with improved messages
      let errorMessage = 'Login failed. Please check your credentials.';
      
      if (err.message) {
        // Use the detailed error message from backend
        errorMessage = err.message;
      } else if (err.code === 'NotAuthorizedException') {
        errorMessage = 'Incorrect email or password. Please check your credentials and try again.';
      } else if (err.code === 'UserNotConfirmedException') {
        errorMessage = 'Your account is not confirmed. Please verify your email address in AWS Cognito Console.';
      } else if (err.code === 'UserNotFoundException') {
        errorMessage = 'User not found. Please check your email address or contact administrator.';
      } else if (err.code === 'PasswordResetRequiredException') {
        errorMessage = 'Password reset required. Please reset your password in AWS Cognito Console.';
      } else if (err.code === 'ForbiddenException') {
        errorMessage = 'Access denied. Please contact administrator.';
      }
      
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h1>Task Manager</h1>
        <p className="login-subtitle">Sign in with your email and password</p>
        <form onSubmit={handleSubmit}>
          {error && (
            <div className="error-message" style={{ 
              color: '#d32f2f', 
              backgroundColor: '#ffebee', 
              padding: '12px', 
              borderRadius: '4px', 
              marginBottom: '16px',
              fontSize: '14px'
            }}>
              {error}
            </div>
          )}
          <div className="form-group">
            <label htmlFor="email">Email Address</label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Enter your email"
              required
              disabled={loading}
              autoComplete="email"
            />
          </div>
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              required
              disabled={loading}
              autoComplete="current-password"
            />
          </div>
          <button 
            type="submit" 
            className="btn-primary" 
            disabled={loading}
          >
            {loading ? 'Signing In...' : 'Sign In'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default Login;

