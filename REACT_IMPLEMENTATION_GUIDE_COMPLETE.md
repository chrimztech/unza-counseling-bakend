/**
 * UNZA Counseling Backend - React TypeScript API Client Implementation Guide
 * 
 * This guide provides a complete implementation for integrating the backend API
 * with a React TypeScript frontend application.
 * 
 * Base URL: http://localhost:8080/api
 * Context Path: /api
 */

 // ============================================================================
 // 1. API TYPES (Create src/types/api.ts)
 // ============================================================================

// Login Request/Response Types
export interface LoginRequest {
  identifier: string;  // email or username
  password: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  user: User;
  expiresIn: number;
}

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  username: string;
  userType: string;
  isActive: boolean;
  roles: Role[];
}

export interface Role {
  id: number;
  name: string;
  description: string;
  permissions: string[];
}

// Dashboard Stats
export interface DashboardStats {
  totalClients: number;
  totalAppointments: number;
  pendingAppointments: number;
  completedSessions: number;
  activeCounselors: number;
  upcomingAppointments: number;
}

// API Response Wrapper
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
  timestamp?: string;
  status?: number;
  path?: string;
}

// Pagination
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

// ============================================================================
 // 2. API CLIENT CONFIGURATION (Create src/lib/api.ts)
 // ============================================================================

import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Create axios instance with default config
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - add auth token
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// Response interceptor - handle errors and token refresh
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    
    // Handle 401 Unauthorized - try to refresh token
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
            refreshToken,
          });
          
          const { token, refreshToken: newRefreshToken } = response.data;
          localStorage.setItem('authToken', token);
          localStorage.setItem('refreshToken', newRefreshToken);
          
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return apiClient(originalRequest);
        } catch (refreshError) {
          // Refresh failed - logout user
          localStorage.removeItem('authToken');
          localStorage.removeItem('refreshToken');
          window.location.href = '/login';
          return Promise.reject(refreshError);
        }
      }
    }
    
    return Promise.reject(error);
  }
);

export default apiClient;

// ============================================================================
 // 3. AUTH API SERVICE (Create src/services/authService.ts)
 // ============================================================================

import apiClient from '../lib/api';
import { LoginRequest, AuthResponse, ApiResponse } from '../types/api';

export const authService = {
  /**
   * Login with email/identifier and password
   * POST /api/auth/login or /api/v1/auth/login
   */
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', credentials);
    return response.data.data!;
  },

  /**
   * Register a new user
   * POST /api/auth/register or /api/v1/auth/register
   */
  async register(userData: {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    userType: string;
  }): Promise<AuthResponse> {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/register', userData);
    return response.data.data!;
  },

  /**
   * Logout user
   * POST /api/auth/logout
   */
  async logout(): Promise<void> {
    await apiClient.post('/auth/logout');
  },

  /**
   * Refresh access token
   * POST /api/auth/refresh
   */
  async refreshToken(refreshToken: string): Promise<AuthResponse> {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/refresh', {
      refreshToken,
    });
    return response.data.data!;
  },

  /**
   * Validate current token
   * GET /api/auth/validate-token
   */
  async validateToken(): Promise<boolean> {
    const token = localStorage.getItem('authToken');
    if (!token) return false;
    
    try {
      const response = await apiClient.get<ApiResponse<{ valid: boolean }>>('/auth/validate-token', {
        headers: { Authorization: `Bearer ${token}` },
      });
      return response.data.data?.valid || false;
    } catch {
      return false;
    }
  },

  /**
   * Get current user profile
   * GET /api/auth/profile
   */
  async getProfile(): Promise<AuthResponse['user']> {
    const response = await apiClient.get<ApiResponse<AuthResponse['user']>>('/auth/profile');
    return response.data.data!;
  },

  /**
   * Request password reset
   * POST /api/auth/password-reset-request
   */
  async requestPasswordReset(email: string): Promise<void> {
    await apiClient.post('/auth/password-reset-request', null, {
      params: { email },
    });
  },

  /**
   * Reset password with token
   * POST /api/auth/password-reset
   */
  async resetPassword(token: string, newPassword: string): Promise<void> {
    await apiClient.post('/auth/password-reset', null, {
      params: { token, newPassword },
    });
  },
};

// ============================================================================
 // 4. DASHBOARD API SERVICE (Create src/services/dashboardService.ts)
 // ============================================================================

import apiClient from '../lib/api';
import { ApiResponse, DashboardStats } from '../types/api';

export const dashboardService = {
  /**
   * Get dashboard statistics
   * GET /dashboard/stats
   */
  async getStats(): Promise<DashboardStats> {
    const response = await apiClient.get<ApiResponse<DashboardStats>>('/dashboard/stats');
    return response.data.data!;
  },
};

// ============================================================================
 // 5. APPOINTMENTS API SERVICE (Create src/services/appointmentService.ts)
 // ============================================================================

import apiClient from '../lib/api';
import { ApiResponse, PaginatedResponse } from '../types/api';

export interface Appointment {
  id: number;
  clientId: number;
  clientName: string;
  counselorId: number;
  counselorName: string;
  appointmentDate: string;
  startTime: string;
  endTime: string;
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED' | 'NO_SHOW';
  type: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAppointmentRequest {
  clientId: number;
  counselorId: number;
  appointmentDate: string;
  startTime: string;
  endTime: string;
  type: string;
  notes?: string;
}

export interface UpdateAppointmentRequest {
  appointmentDate?: string;
  startTime?: string;
  endTime?: string;
  type?: string;
  notes?: string;
}

export const appointmentService = {
  /**
   * Get all appointments with pagination
   * GET /api/appointments
   */
  async getAllAppointments(params: {
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<PaginatedResponse<Appointment>> {
    const response = await apiClient.get<ApiResponse<PaginatedResponse<Appointment>>>('/appointments', {
      params: { ...params, page: params.page || 0, size: params.size || 20 },
    });
    return response.data.data!;
  },

  /**
   * Get appointment by ID
   * GET /api/appointments/{id}
   */
  async getAppointmentById(id: number): Promise<Appointment> {
    const response = await apiClient.get<ApiResponse<Appointment>>(`/appointments/${id}`);
    return response.data.data!;
  },

  /**
   * Get appointments by client ID
   * GET /api/appointments/client/{clientId}
   */
  async getAppointmentsByClient(clientId: number, params?: {
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<Appointment>> {
    const response = await apiClient.get<ApiResponse<PaginatedResponse<Appointment>>>(
      `/appointments/client/${clientId}`,
      { params: { ...params, page: params?.page || 0, size: params?.size || 20 } }
    );
    return response.data.data!;
  },

  /**
   * Get upcoming appointments
   * GET /api/appointments/upcoming
   */
  async getUpcomingAppointments(params?: {
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<Appointment>> {
    const response = await apiClient.get<ApiResponse<PaginatedResponse<Appointment>>>(
      '/appointments/upcoming',
      { params: { ...params, page: params?.page || 0, size: params?.size || 20 } }
    );
    return response.data.data!;
  },

  /**
   * Get today's appointments
   * GET /api/appointments/today
   */
  async getTodaysAppointments(params?: {
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<Appointment>> {
    const response = await apiClient.get<ApiResponse<PaginatedResponse<Appointment>>>(
      '/appointments/today',
      { params: { ...params, page: params?.page || 0, size: params?.size || 20 } }
    );
    return response.data.data!;
  },

  /**
   * Get pending appointments
   * GET /api/appointments/pending
   */
  async getPendingAppointments(params?: {
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<Appointment>> {
    const response = await apiClient.get<ApiResponse<PaginatedResponse<Appointment>>>(
      '/appointments/pending',
      { params: { ...params, page: params?.page || 0, size: params?.size || 20 } }
    );
    return response.data.data!;
  },

  /**
   * Create a new appointment
   * POST /api/appointments
   */
  async createAppointment(data: CreateAppointmentRequest): Promise<Appointment> {
    const response = await apiClient.post<ApiResponse<Appointment>>('/appointments', data);
    return response.data.data!;
  },

  /**
   * Update an appointment
   * PUT /api/appointments/{id}
   */
  async updateAppointment(id: number, data: UpdateAppointmentRequest): Promise<Appointment> {
    const response = await apiClient.put<ApiResponse<Appointment>>(`/appointments/${id}`, data);
    return response.data.data!;
  },

  /**
   * Cancel an appointment
   * PUT /api/appointments/{id}/cancel
   */
  async cancelAppointment(id: number): Promise<Appointment> {
    const response = await apiClient.put<ApiResponse<Appointment>>(`/appointments/${id}/cancel`);
    return response.data.data!;
  },

  /**
   * Confirm an appointment
   * PUT /api/appointments/{id}/confirm
   */
  async confirmAppointment(id: number): Promise<Appointment> {
    const response = await apiClient.put<ApiResponse<Appointment>>(`/appointments/${id}/confirm`);
    return response.data.data!;
  },

  /**
   * Reschedule an appointment
   * PUT /api/appointments/{id}/reschedule
   */
  async rescheduleAppointment(id: number, data: UpdateAppointmentRequest): Promise<Appointment> {
    const response = await apiClient.put<ApiResponse<Appointment>>(
      `/appointments/${id}/reschedule`,
      data
    );
    return response.data.data!;
  },

  /**
   * Check counselor availability
   * GET /api/appointments/availability
   */
  async checkAvailability(params: {
    counselorId: number;
    date: string;  // Format: YYYY-MM-DD
  }): Promise<{ availableSlots: string[] }> {
    const response = await apiClient.get<ApiResponse<{ availableSlots: string[] }>>(
      '/appointments/availability',
      { params }
    );
    return response.data.data!;
  },
};

// ============================================================================
 // 6. CUSTOM HOOKS (Create src/hooks/useAuth.ts)
 // ============================================================================

import { useState, useEffect, useCallback } from 'react';
import { authService } from '../services/authService';
import { User } from '../types/api';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

export function useAuth() {
  const [state, setState] = useState<AuthState>({
    user: null,
    isAuthenticated: false,
    isLoading: true,
    error: null,
  });

  const checkAuth = useCallback(async () => {
    const token = localStorage.getItem('authToken');
    if (!token) {
      setState({ user: null, isAuthenticated: false, isLoading: false, error: null });
      return;
    }

    try {
      const isValid = await authService.validateToken();
      if (isValid) {
        const user = await authService.getProfile();
        setState({ user, isAuthenticated: true, isLoading: false, error: null });
      } else {
        localStorage.removeItem('authToken');
        localStorage.removeItem('refreshToken');
        setState({ user: null, isAuthenticated: false, isLoading: false, error: null });
      }
    } catch {
      setState({ user: null, isAuthenticated: false, isLoading: false, error: null });
    }
  }, []);

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  const login = async (identifier: string, password: string) => {
    setState((prev) => ({ ...prev, isLoading: true, error: null }));
    try {
      const response = await authService.login({ identifier, password });
      localStorage.setItem('authToken', response.token);
      localStorage.setItem('refreshToken', response.refreshToken);
      setState({
        user: response.user,
        isAuthenticated: true,
        isLoading: false,
        error: null,
      });
      return response;
    } catch (error: any) {
      const errorMessage = error.response?.data?.error || 'Login failed';
      setState((prev) => ({ ...prev, isLoading: false, error: errorMessage }));
      throw new Error(errorMessage);
    }
  };

  const logout = async () => {
    try {
      await authService.logout();
    } finally {
      localStorage.removeItem('authToken');
      localStorage.removeItem('refreshToken');
      setState({ user: null, isAuthenticated: false, isLoading: false, error: null });
    }
  };

  return {
    ...state,
    login,
    logout,
    checkAuth,
  };
}

// ============================================================================
 // 7. LOGIN COMPONENT EXAMPLE (Create src/components/Login.tsx)
 // ============================================================================

import React, { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { useNavigate } from 'react-router-dom';

const Login: React.FC = () => {
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const { login, isLoading, error } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await login(identifier, password);
      navigate('/dashboard');
    } catch (err) {
      // Error is handled by useAuth hook
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            UNZA Counseling System
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            Sign in to your account
          </p>
        </div>
        
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
              {error}
            </div>
          )}
          
          <div className="rounded-md shadow-sm -space-y-px">
            <div>
              <label htmlFor="identifier" className="sr-only">
                Email or Username
              </label>
              <input
                id="identifier"
                name="identifier"
                type="text"
                required
                className="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
                placeholder="Email or Username"
                value={identifier}
                onChange={(e) => setIdentifier(e.target.value)}
              />
            </div>
            <div>
              <label htmlFor="password" className="sr-only">
                Password
              </label>
              <input
                id="password"
                name="password"
                type={showPassword ? 'text' : 'password'}
                required
                className="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
          </div>

          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <input
                id="show-password"
                name="show-password"
                type="checkbox"
                className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                checked={showPassword}
                onChange={(e) => setShowPassword(e.target.checked)}
              />
              <label htmlFor="show-password" className="ml-2 block text-sm text-gray-900">
                Show password
              </label>
            </div>

            <div className="text-sm">
              <a href="/forgot-password" className="font-medium text-indigo-600 hover:text-indigo-500">
                Forgot your password?
              </a>
            </div>
          </div>

          <div>
            <button
              type="submit"
              disabled={isLoading}
              className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
            >
              {isLoading ? 'Signing in...' : 'Sign in'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default Login;

// ============================================================================
 // 8. DASHBOARD COMPONENT EXAMPLE (Create src/components/Dashboard.tsx)
 // ============================================================================

import React, { useEffect, useState } from 'react';
import { dashboardService } from '../services/dashboardService';
import { DashboardStats } from '../types/api';

const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const data = await dashboardService.getStats();
        setStats(data);
      } catch (err: any) {
        setError(err.response?.data?.error || 'Failed to load dashboard data');
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-4 bg-red-100 border border-red-400 text-red-700 rounded">
        {error}
      </div>
    );
  }

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-6">Dashboard</h1>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {/* Stats Cards */}
        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <svg className="h-6 w-6 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Total Clients</dt>
                  <dd className="text-lg font-medium text-gray-900">{stats?.totalClients || 0}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <svg className="h-6 w-6 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Total Appointments</dt>
                  <dd className="text-lg font-medium text-gray-900">{stats?.totalAppointments || 0}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <svg className="h-6 w-6 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Pending</dt>
                  <dd className="text-lg font-medium text-gray-900">{stats?.pendingAppointments || 0}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <svg className="h-6 w-6 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Completed Sessions</dt>
                  <dd className="text-lg font-medium text-gray-900">{stats?.completedSessions || 0}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <svg className="h-6 w-6 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Active Counselors</dt>
                  <dd className="text-lg font-medium text-gray-900">{stats?.activeCounselors || 0}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <svg className="h-6 w-6 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                </svg>
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Upcoming</dt>
                  <dd className="text-lg font-medium text-gray-900">{stats?.upcomingAppointments || 0}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;

// ============================================================================
 // 9. APPOINTMENTS LIST COMPONENT (Create src/components/AppointmentsList.tsx)
 // ============================================================================

import React, { useEffect, useState } from 'react';
import { appointmentService, Appointment } from '../services/appointmentService';

const AppointmentsList: React.FC = () => {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
  });

  const fetchAppointments = async (page: number = 0) => {
    setLoading(true);
    try {
      const response = await appointmentService.getAllAppointments({
        page,
        size: pagination.size,
      });
      setAppointments(response.content);
      setPagination((prev) => ({
        ...prev,
        page,
        totalElements: response.totalElements,
        totalPages: response.totalPages,
      }));
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to load appointments');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAppointments();
  }, []);

  const handlePageChange = (newPage: number) => {
    fetchAppointments(newPage);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'CONFIRMED':
        return 'bg-green-100 text-green-800';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      case 'COMPLETED':
        return 'bg-blue-100 text-blue-800';
      case 'NO_SHOW':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  if (loading && appointments.length === 0) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-4 bg-red-100 border border-red-400 text-red-700 rounded">
        {error}
      </div>
    );
  }

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Appointments</h1>
        <button className="bg-indigo-600 text-white px-4 py-2 rounded-md hover:bg-indigo-700">
          New Appointment
        </button>
      </div>

      <div className="bg-white shadow overflow-hidden sm:rounded-lg">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Client
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Counselor
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Date & Time
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Type
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Status
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {appointments.map((appointment) => (
              <tr key={appointment.id}>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm font-medium text-gray-900">{appointment.clientName}</div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-gray-500">{appointment.counselorName}</div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-gray-900">{appointment.appointmentDate}</div>
                  <div className="text-sm text-gray-500">
                    {appointment.startTime} - {appointment.endTime}
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className="text-sm text-gray-500">{appointment.type}</span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(appointment.status)}`}>
                    {appointment.status}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                  <button className="text-indigo-600 hover:text-indigo-900 mr-3">
                    View
                  </button>
                  <button className="text-gray-600 hover:text-gray-900">
                    Edit
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      <div className="flex justify-between items-center mt-4">
        <div className="text-sm text-gray-700">
          Showing {pagination.page * pagination.size + 1} to{' '}
          {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)} of{' '}
          {pagination.totalElements} appointments
        </div>
        <div className="flex space-x-2">
          <button
            onClick={() => handlePageChange(pagination.page - 1)}
            disabled={pagination.page === 0}
            className="px-3 py-1 border border-gray-300 rounded-md text-sm disabled:opacity-50"
          >
            Previous
          </button>
          <button
            onClick={() => handlePageChange(pagination.page + 1)}
            disabled={pagination.page >= pagination.totalPages - 1}
            className="px-3 py-1 border border-gray-300 rounded-md text-sm disabled:opacity-50"
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
};

export default AppointmentsList;

// ============================================================================
 // 10. API ENDPOINTS SUMMARY
 // ============================================================================

/**
 * Complete API Endpoints Summary:
 * 
 * AUTHENTICATION (/api/auth or /api/v1/auth)
 * - POST   /login                    - Login with identifier and password
 * - POST   /register                 - Register new user
 * - POST   /logout                   - Logout user
 * - POST   /refresh                  - Refresh access token
 * - GET    /profile                  - Get current user profile
 * - GET    /validate-token           - Validate current token
 * - POST   /password-reset-request   - Request password reset email
 * - POST   /password-reset           - Reset password with token
 * - POST   /verify-email             - Verify email with token
 * 
 * DASHBOARD (/dashboard)
 * - GET    /stats                    - Get dashboard statistics
 * 
 * APPOINTMENTS (/api/appointments)
 * - GET    /                         - Get all appointments (paginated)
 * - GET    /{id}                     - Get appointment by ID
 * - GET    /client/{clientId}        - Get appointments by client
 * - GET    /upcoming                 - Get upcoming appointments
 * - GET    /today                    - Get today's appointments
 * - GET    /pending                  - Get pending appointments
 * - GET    /cancelled                - Get cancelled appointments
 * - GET    /confirmed                - Get confirmed appointments
 * - GET    /availability             - Check counselor availability
 * - GET    /stats                    - Get appointment statistics
 * - POST   /                         - Create new appointment
 * - PUT    /{id}                     - Update appointment
 * - PUT    /{id}/cancel              - Cancel appointment
 * - PUT    /{id}/confirm             - Confirm appointment
 * - PUT    /{id}/reschedule          - Reschedule appointment
 * - GET    /export                   - Export appointments
 * 
 * USERS (/api/users)
 * - GET    /                         - Get all users (paginated)
 * - GET    /{id}                     - Get user by ID
 * - PUT    /{id}                     - Update user
 * - DELETE /{id}                     - Delete user
 * 
 * CLIENTS (/api/clients)
 * - GET    /                         - Get all clients
 * - GET    /{id}                     - Get client by ID
 * - POST   /                         - Create new client
 * - PUT    /{id}                     - Update client
 * 
 * COUNSELORS (/api/counselors)
 * - GET    /                         - Get all counselors
 * - GET    /{id}                     - Get counselor by ID
 * - POST   /                         - Create new counselor
 * - PUT    /{id}                     - Update counselor
 * 
 * SESSIONS (/api/sessions)
 * - GET    /                         - Get all sessions
 * - GET    /{id}                     - Get session by ID
 * - POST   /                         - Create new session
 * - PUT    /{id}                     - Update session
 * - PUT    /{id}/complete            - Complete session
 * 
 * NOTIFICATIONS (/api/notifications)
 * - GET    /user/{userId}            - Get user notifications
 * - POST   /                         - Create notification
 * - PUT    /{id}/read                - Mark as read
 * 
 * MESSAGES (/api/messages)
 * - GET    /                         - Get all messages
 * - GET    /{id}                     - Get message by ID
 * - POST   /                         - Send message
 * - DELETE /{id}                     - Delete message
 * 
 * RISK ASSESSMENTS (/api/risk-assessments)
 * - GET    /                         - Get all assessments
 * - GET    /{id}                     - Get assessment by ID
 * - POST   /                         - Create assessment
 * - PUT    /{id}                     - Update assessment
 * 
 * CONSENT FORMS (/api/consent-forms)
 * - GET    /                         - Get all consent forms
 * - GET    /{id}                     - Get consent form by ID
 * - POST   /                         - Create consent form
 * - PUT    /{id}                     - Update consent form
 * - POST   /sign                     - Sign consent form
 */

// ============================================================================
// 11. INSTALLATION INSTRUCTIONS
// ============================================================================

/**
 * To use these files in your React TypeScript project:
 * 
 * 1. Install dependencies:
 *    npm install axios react-router-dom
 * 
 * 2. Install type definitions (if needed):
 *    npm install --save-dev @types/react-router-dom
 * 
 * 3. Copy the files to your project:
 *    - src/types/api.ts
 *    - src/lib/api.ts
 *    - src/services/authService.ts
 *    - src/services/dashboardService.ts
 *    - src/services/appointmentService.ts
 *    - src/hooks/useAuth.ts
 *    - src/components/Login.tsx
 *    - src/components/Dashboard.tsx
 *    - src/components/AppointmentsList.tsx
 * 
 * 4. Update your App.tsx to include routing:
 * 
 *    import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
 *    import { useAuth } from './hooks/useAuth';
 *    import Login from './components/Login';
 *    import Dashboard from './components/Dashboard';
 * 
 *    const PrivateRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
 *      const { isAuthenticated, isLoading } = useAuth();
 *      if (isLoading) return <div>Loading...</div>;
 *      return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
 *    };
 * 
 *    function App() {
 *      return (
 *        <BrowserRouter>
 *          <Routes>
 *            <Route path="/login" element={<Login />} />
 *            <Route
 *              path="/dashboard"
 *              element={
 *                <PrivateRoute>
 *                  <Dashboard />
 *                </PrivateRoute>
 *              }
 *            />
 *            <Route path="*" element={<Navigate to="/dashboard" />} />
 *          </Routes>
 *        </BrowserRouter>
 *      );
 *    }
 * 
 *    export default App;
 */
