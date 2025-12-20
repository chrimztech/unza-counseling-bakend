import api, { endpoints } from './api';
import { LoginRequest, RegisterRequest, AuthResponse, User } from '../types/api';

// Auth Service
export const authService = {
  // Login
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post(endpoints.auth.login, credentials);
    return response.data;
  },

  // Register
  register: async (userData: RegisterRequest): Promise<AuthResponse> => {
    const response = await api.post(endpoints.auth.register, userData);
    return response.data;
  },

  // Logout
  logout: async (): Promise<void> => {
    await api.post(endpoints.auth.logout);
    localStorage.removeItem('authToken');
  },

  // Get current user profile
  getProfile: async (): Promise<User> => {
    const response = await api.get(endpoints.auth.profile);
    return response.data;
  },

  // Refresh token
  refreshToken: async (): Promise<AuthResponse> => {
    const response = await api.post(endpoints.auth.refresh);
    return response.data;
  },

  // Check if user is authenticated
  isAuthenticated: (): boolean => {
    const token = localStorage.getItem('authToken');
    return !!token;
  },

  // Get stored token
  getToken: (): string | null => {
    return localStorage.getItem('authToken');
  },

  // Set token
  setToken: (token: string): void => {
    localStorage.setItem('authToken', token);
  },

  // Remove token
  removeToken: (): void => {
    localStorage.removeItem('authToken');
  },
};