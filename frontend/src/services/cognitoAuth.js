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
      // Handle errors
      const error = new Error(data.message || 'Authentication failed');
      error.code = response.status === 401 ? 'NotAuthorizedException' : 'UnknownError';
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

