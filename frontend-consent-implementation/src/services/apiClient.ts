/**
 * API Client - Centralized Axios configuration for API calls
 */
import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';

// API Base URL - Change this to match your backend URL
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Create axios instance
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

// Request interceptor to add auth token
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Get token from localStorage
    const token = localStorage.getItem('token');
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log('Axios interceptor - Token from localStorage: Present');
    } else {
      console.log('Axios interceptor - Token from localStorage: Not found');
    }
    
    return config;
  },
  (error: AxiosError) => {
    console.error('Axios interceptor - Request error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    console.error('Axios interceptor - Response error:', error.message);
    
    if (error.response) {
      // Server responded with error
      const status = error.response.status;
      
      if (status === 401) {
        // Unauthorized - Token expired or invalid
        console.log('Axios interceptor - 401 Unauthorized, clearing auth data');
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        
        // Redirect to login if not already there
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login';
        }
      } else if (status === 403) {
        console.error('Axios interceptor - 403 Forbidden');
      } else if (status === 404) {
        console.error('Axios interceptor - 404 Not Found');
      } else if (status >= 500) {
        console.error('Axios interceptor - Server error:', status);
      }
    } else if (error.request) {
      // Request made but no response
      console.error('Axios interceptor - No response received:', error.message);
    }
    
    return Promise.reject(error);
  }
);

export default apiClient;
