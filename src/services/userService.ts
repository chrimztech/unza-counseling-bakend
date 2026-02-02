import api from './api';
import { PaginatedResponse, User, RegisterRequest } from '../types/api';

/**
 * User Service - Handles all user-related API operations
 * Required Roles: ADMIN (full access), COUNSELOR (view only)
 */
export const userService = {
  /**
   * Get all users with pagination
   * GET /api/v1/users?page=0&size=10
   */
  getAllUsers: async (page = 0, size = 10): Promise<PaginatedResponse<User>> => {
    const response = await api.get(`/users?page=${page}&size=${size}`);
    return response.data.data;
  },

  /**
   * Get user by ID
   * GET /api/v1/users/{id}
   */
  getUserById: async (id: string): Promise<User> => {
    const response = await api.get(`/users/${id}`);
    return response.data.data;
  },

  /**
   * Get user by email
   * GET /api/v1/users/email/{email}
   */
  getUserByEmail: async (email: string): Promise<User> => {
    const response = await api.get(`/users/email/${email}`);
    return response.data.data;
  },

  /**
   * Create new user (Admin only)
   * POST /api/v1/users
   */
  createUser: async (userData: RegisterRequest): Promise<User> => {
    const response = await api.post('/users', userData);
    return response.data.data;
  },

  /**
   * Update user (Admin only)
   * PUT /api/v1/users/{id}
   */
  updateUser: async (id: string, userData: Partial<User>): Promise<User> => {
    const response = await api.put(`/users/${id}`, userData);
    return response.data.data;
  },

  /**
   * Delete user (Admin only)
   * DELETE /api/v1/users/{id}
   */
  deleteUser: async (id: string): Promise<void> => {
    await api.delete(`/users/${id}`);
  },

  /**
   * Get users by role
   * GET /api/v1/users/role/{role}
   */
  getUsersByRole: async (role: string): Promise<User[]> => {
    const response = await api.get(`/users/role/${role}`);
    return response.data.data;
  },

  /**
   * Get total user count
   * GET /api/v1/users/count
   */
  getUserCount: async (): Promise<number> => {
    const response = await api.get('/users/count');
    return response.data.data;
  },

  /**
   * Search users
   * GET /api/v1/users/search?query={query}&page=0&size=10
   */
  searchUsers: async (query: string, page = 0, size = 10): Promise<PaginatedResponse<User>> => {
    const response = await api.get(`/users/search?query=${query}&page=${page}&size=${size}`);
    return response.data.data;
  },

  /**
   * Get active users
   * GET /api/v1/users/active
   */
  getActiveUsers: async (page = 0, size = 10): Promise<PaginatedResponse<User>> => {
    const response = await api.get(`/users/active?page=${page}&size=${size}`);
    return response.data.data;
  },

  /**
   * Get inactive users
   * GET /api/v1/users/inactive
   */
  getInactiveUsers: async (page = 0, size = 10): Promise<PaginatedResponse<User>> => {
    const response = await api.get(`/users/inactive?page=${page}&size=${size}`);
    return response.data.data;
  },

  /**
   * Activate user (Admin only)
   * PUT /api/v1/users/{id}/activate
   */
  activateUser: async (id: string): Promise<User> => {
    const response = await api.put(`/users/${id}/activate`);
    return response.data.data;
  },

  /**
   * Deactivate user (Admin only)
   * PUT /api/v1/users/{id}/deactivate
   */
  deactivateUser: async (id: string): Promise<User> => {
    const response = await api.put(`/users/${id}/deactivate`);
    return response.data.data;
  },

  /**
   * Get current user profile
   * GET /api/v1/users/profile
   */
  getCurrentProfile: async (): Promise<User> => {
    const response = await api.get('/users/profile');
    return response.data.data;
  },

  /**
   * Get all available roles
   * GET /api/v1/users/roles
   */
  getAllRoles: async (): Promise<string[]> => {
    const response = await api.get('/users/roles');
    return response.data.data;
  },

  /**
   * Get user count by role
   * GET /api/v1/users/count-by-role
   */
  getUserCountByRole: async (): Promise<Record<string, number>> => {
    const response = await api.get('/users/count-by-role');
    return response.data.data;
  },

  /**
   * Change user password (Admin only)
   * PUT /api/v1/users/{id}/password?newPassword={password}
   */
  changeUserPassword: async (id: string, newPassword: string): Promise<void> => {
    await api.put(`/users/${id}/password?newPassword=${newPassword}`);
  },

  /**
   * Export users to CSV/PDF (Admin only)
   * GET /api/v1/users/export?format=csv
   */
  exportUsers: async (format: 'csv' | 'pdf' = 'csv'): Promise<Blob> => {
    const response = await api.get(`/users/export?format=${format}`, {
      responseType: 'blob'
    });
    return response.data;
  }
};
