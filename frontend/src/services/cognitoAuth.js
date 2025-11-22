/**
 * Authenticate user with email and password
 * Calls the backend API which handles Cognito authentication
 * @param {string} email - User's email address
 * @param {string} password - User's password
 * @returns {Promise<string>} - ID Token
 */
export const authenticateUser = async (email, password) => {
  try {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email: email,
        password: password,
      }),
    });

    const data = await response.json();

    if (!response.ok) {
      // Handle errors with improved error messages
      const errorMessage = data.message || 'Authentication failed';
      const error = new Error(errorMessage);
      
      // Set error code based on status and message
      if (response.status === 401) {
        error.code = 'NotAuthorizedException';
      } else if (response.status === 403) {
        if (errorMessage.includes('not confirmed') || errorMessage.includes('verify')) {
          error.code = 'UserNotConfirmedException';
        } else if (errorMessage.includes('Temporary password') || errorMessage.includes('reset required')) {
          error.code = 'PasswordResetRequiredException';
        } else {
          error.code = 'ForbiddenException';
        }
      } else if (response.status === 404) {
        error.code = 'UserNotFoundException';
      } else {
        error.code = 'UnknownError';
      }
      
      // Include error code if available
      if (data.errorCode) {
        error.errorCode = data.errorCode;
      }
      
      throw error;
    }

    // Extract ID token from response
    if (data && data.idToken) {
      return data.idToken;
    } else {
      throw new Error('No ID token received from server');
    }
  } catch (err) {
    // Handle network errors or other issues
    if (err.code) {
      throw err; // Re-throw known errors
    }
    if (err.message && err.message.includes('fetch')) {
      throw new Error('Network error. Please check your connection and ensure the backend is running.');
    }
    throw err;
  }
};

/**
 * Sign out the current user
 * This clears the token from localStorage
 */
export const signOut = () => {
  localStorage.removeItem('cognito_id_token');
};

/**
 * Get the current user's ID token from localStorage
 * @returns {string|null}
 */
export const getCurrentUserToken = () => {
  return localStorage.getItem('cognito_id_token');
};

