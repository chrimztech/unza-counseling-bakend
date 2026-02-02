# UNZA Counseling System - Frontend Integration Guide

## Overview
This document provides a complete guide for integrating the UNZA Counseling Management System backend API with your React frontend. The backend runs on Spring Boot at `http://localhost:8080`.

**Base API URL:** `http://localhost:8080/api/v1`

**Deadline:** Tuesday - All features must be implemented

---

## Quick Start Checklist

- [ ] Set up environment variables
- [ ] Implement authentication (login/logout/refresh)
- [ ] Create role-based routing (Admin/Counselor/Student dashboards)
- [ ] Implement dashboard with statistics
- [ ] Build client management pages
- [ ] Build appointment management
- [ ] Build session notes functionality
- [ ] Implement risk assessments
- [ ] Build notifications system
- [ ] Test all endpoints

---

## Table of Contents
1. [Setup & Configuration](#1-setup--configuration)
2. [Authentication Endpoints](#2-authentication-endpoints)
3. [User Management Endpoints](#3-user-management-endpoints)
4. [Client Management Endpoints](#4-client-management-endpoints)
5. [Appointment Management Endpoints](#5-appointment-management-endpoints)
6. [Session Management Endpoints](#6-session-management-endpoints)
7. [Counselor Endpoints](#7-counselor-endpoints)
8. [Assessment Endpoints](#8-assessment-endpoints)
9. [Academic Performance Endpoints](#9-academic-performance-endpoints)
10. [Risk Assessment Endpoints](#10-risk-assessment-endpoints)
11. [Dashboard & Analytics Endpoints](#11-dashboard--analytics-endpoints)
12. [Report Endpoints](#12-report-endpoints)
13. [Notification Endpoints](#13-notification-endpoints)
14. [Message Endpoints](#14-message-endpoints)
15. [Resource Endpoints](#15-resource-endpoints)
16. [Settings Endpoints](#16-settings-endpoints)
17. [Consent Form Endpoints](#17-consent-form-endpoints)
18. [Admin Endpoints](#18-admin-endpoints)
19. [TypeScript Types](#19-typescript-types)
20. [Error Handling](#20-error-handling)
21. [Role-Based Access Control](#21-role-based-access-control)

---

## 1. Setup & Configuration

### Environment Variables
Create a `.env` file in your React project root:

```env
REACT_APP_API_URL=http://localhost:8080/api/v1
REACT_APP_ENVIRONMENT=development
```

### Existing API Configuration (Already in your project)
Your `src/services/api.ts` is already configured correctly:

```typescript
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/v1';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

### Authentication Flow
1. User logs in with credentials
2. Backend returns JWT token + user data
3. Store token in `localStorage`
4. Include token in all subsequent requests via Authorization header
5. On 401 error, redirect to login

---

## 2. Authentication Endpoints

### POST `/auth/login` - User Login
**Frontend Implementation:** Already exists in `src/services/authService.ts`

```typescript
// src/services/authService.ts
import api from './api';
import { LoginRequest, RegisterRequest, AuthResponse, User } from '../types/api';

export const authService = {
  // Login
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post('/auth/login', credentials);
    // Store token after successful login
    if (response.data.token) {
      localStorage.setItem('authToken', response.data.token);
    }
    return response.data;
  },

  // Register
  register: async (userData: RegisterRequest): Promise<AuthResponse> => {
    const response = await api.post('/auth/register', userData);
    return response.data;
  },

  // Logout
  logout: async (): Promise<void> => {
    await api.post('/auth/logout');
    localStorage.removeItem('authToken');
  },

  // Get current user profile
  getProfile: async (): Promise<User> => {
    const response = await api.get('/auth/profile');
    return response.data;
  },

  // Refresh token
  refreshToken: async (refreshToken: string): Promise<AuthResponse> => {
    const response = await api.post('/auth/refresh', { refreshToken });
    if (response.data.token) {
      localStorage.setItem('authToken', response.data.token);
    }
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

  // ADDITIONAL ENDPOINTS TO ADD:

  // Anonymous login
  anonymousLogin: async (deviceIdentifier: string) => {
    const response = await api.post('/auth/anonymous-login', {
      deviceIdentifier
    });
    return response.data;
  },

  // Request password reset
  requestPasswordReset: async (email: string) => {
    const response = await api.post(`/auth/password-reset-request?email=${email}`);
    return response.data;
  },

  // Reset password
  resetPassword: async (token: string, newPassword: string) => {
    const response = await api.post(
      `/auth/password-reset?token=${token}&newPassword=${newPassword}`
    );
    return response.data;
  },

  // Verify email
  verifyEmail: async (token: string) => {
    const response = await api.post(`/auth/verify-email?token=${token}`);
    return response.data;
  },

  // Resend verification email
  resendVerificationEmail: async (email: string) => {
    const response = await api.post(`/auth/resend-verification?email=${email}`);
    return response.data;
  },

  // Validate token
  validateToken: async (token: string) => {
    const response = await api.get('/auth/validate-token', {
      headers: { Authorization: `Bearer ${token}` }
    });
    return response.data.valid;
  },

  // Logout from all devices
  logoutAllDevices: async () => {
    const response = await api.post('/auth/logout-all');
    localStorage.removeItem('authToken');
    return response.data;
  }
};
```

**Request Body (Login):**
```json
{
  "identifier": "admin@unza.zm",
  "password": "Admin@123"
}
```

**Response (Login):**
```json
{
  "token": "eyJhbGciOiJIUzI1Ni...",
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2g...",
  "user": {
    "id": 1,
    "email": "admin@unza.zm",
    "firstName": "Admin",
    "lastName": "User",
    "roles": [{"name": "ROLE_ADMIN"}],
    "active": true
  },
  "expiresIn": 86400
}
```

---

## 3. User Management Endpoints

**Required Roles:** ADMIN, COUNSELOR (varies by endpoint)

```typescript
// Create src/services/userService.ts
import api from './api';
import { PaginatedResponse, User } from '../types/api';

export const userService = {
  // Get all users (paginated)
  getAllUsers: async (page = 0, size = 10): Promise<PaginatedResponse<User>> => {
    const response = await api.get(`/users?page=${page}&size=${size}`);
    return response.data.data;
  },

  // Get user by ID
  getUserById: async (id: string): Promise<User> => {
    const response = await api.get(`/users/${id}`);
    return response.data.data;
  },

  // Get user by email
  getUserByEmail: async (email: string): Promise<User> => {
    const response = await api.get(`/users/email/${email}`);
    return response.data.data;
  },

  // Create user (Admin only)
  createUser: async (userData: RegisterRequest): Promise<User> => {
    const response = await api.post('/users', userData);
    return response.data.data;
  },

  // Update user (Admin only)
  updateUser: async (id: string, userData: Partial<User>): Promise<User> => {
    const response = await api.put(`/users/${id}`, userData);
    return response.data.data;
  },

  // Delete user (Admin only)
  deleteUser: async (id: string): Promise<void> => {
    await api.delete(`/users/${id}`);
  },

  // Get users by role
  getUsersByRole: async (role: string): Promise<User[]> => {
    const response = await api.get(`/users/role/${role}`);
    return response.data.data;
  },

  // Get user count
  getUserCount: async (): Promise<number> => {
    const response = await api.get('/users/count');
    return response.data.data;
  },

  // Search users
  searchUsers: async (query: string, page = 0, size = 10): Promise<PaginatedResponse<User>> => {
    const response = await api.get(`/users/search?query=${query}&page=${page}&size=${size}`);
    return response.data.data;
  },

  // Get active users
  getActiveUsers: async (page = 0, size = 10): Promise<PaginatedResponse<User>> => {
    const response = await api.get(`/users/active?page=${page}&size=${size}`);
    return response.data.data;
  },

  // Get inactive users
  getInactiveUsers: async (page = 0, size = 10): Promise<PaginatedResponse<User>> => {
    const response = await api.get(`/users/inactive?page=${page}&size=${size}`);
    return response.data.data;
  },

  // Activate user
  activateUser: async (id: string): Promise<User> => {
    const response = await api.put(`/users/${id}/activate`);
    return response.data.data;
  },

  // Deactivate user
  deactivateUser: async (id: string): Promise<User> => {
    const response = await api.put(`/users/${id}/deactivate`);
    return response.data.data;
  },

  // Get current user profile
  getCurrentProfile: async (): Promise<User> => {
    const response = await api.get('/users/profile');
    return response.data.data;
  },

  // Get all roles
  getAllRoles: async (): Promise<string[]> => {
    const response = await api.get('/users/roles');
    return response.data.data;
  },

  // Get user count by role
  getUserCountByRole: async (): Promise<Record<string, number>> => {
    const response = await api.get('/users/count-by-role');
    return response.data.data;
  },

  // Change user password (Admin only)
  changeUserPassword: async (id: string, newPassword: string): Promise<void> => {
    await api.put(`/users/${id}/password?newPassword=${newPassword}`);
  },

  // Export users (Admin only)
  exportUsers: async (format: 'csv' | 'pdf' = 'csv'): Promise<Blob> => {
    const response = await api.get(`/users/export?format=${format}`, {
      responseType: 'blob'
    });
    return response.data;
  }
};
```

---

## 4. Client Management Endpoints

**Required Roles:** ADMIN, COUNSELOR

```typescript
// Update src/services/clientService.ts
import api from './api';
import { Client, PaginatedResponse } from '../types/api';

export const clientService = {
  // Get all clients with pagination and filtering
  getClients: async (params?: {
    page?: number;
    size?: number;
    sortBy?: string;
    sortDirection?: 'ASC' | 'DESC';
    search?: string;
    status?: 'ACTIVE' | 'INACTIVE' | 'COMPLETED' | 'REFERRED' | 'ON_HOLD' | 'WITHDRAWN';
    riskLevel?: 'LOW' | 'MODERATE' | 'HIGH' | 'CRITICAL';
  }): Promise<PaginatedResponse<Client>> => {
    const queryParams = new URLSearchParams();
    if (params?.page !== undefined) queryParams.append('page', String(params.page));
    if (params?.size !== undefined) queryParams.append('size', String(params.size));
    if (params?.sortBy) queryParams.append('sortBy', params.sortBy);
    if (params?.sortDirection) queryParams.append('sortDirection', params.sortDirection);
    if (params?.search) queryParams.append('search', params.search);
    if (params?.status) queryParams.append('status', params.status);
    if (params?.riskLevel) queryParams.append('riskLevel', params.riskLevel);

    const response = await api.get(`/clients?${queryParams.toString()}`);
    return response.data; // Returns Page<Client> directly
  },

  // Get client by ID
  getClient: async (id: string): Promise<Client> => {
    const response = await api.get(`/clients/${id}`);
    return response.data;
  },

  // Get client by student ID
  getClientByStudentId: async (studentId: string): Promise<Client> => {
    const response = await api.get(`/clients/student/${studentId}`);
    return response.data;
  },

  // Update client
  updateClient: async (id: string, clientData: Partial<Client>): Promise<Client> => {
    const response = await api.put(`/clients/${id}`, clientData);
    return response.data;
  },

  // Get client statistics
  getClientStats: async (): Promise<{ activeClients: number; highRiskClients: number }> => {
    const response = await api.get('/clients/stats');
    return response.data.data;
  },

  // Update client risk level
  updateRiskLevel: async (id: string, riskLevel: 'LOW' | 'MODERATE' | 'HIGH' | 'CRITICAL'): Promise<Client> => {
    const response = await api.put(`/clients/${id}/risk-level?riskLevel=${riskLevel}`);
    return response.data;
  }
};
```

---

## 5. Appointment Management Endpoints

**Required Roles:** All authenticated users

```typescript
// Create src/services/appointmentService.ts
import api from './api';

export interface Appointment {
  id: string;
  title: string;
  student: any;
  counselor: any;
  appointmentDate: string;
  duration: number;
  type: 'INITIAL_CONSULTATION' | 'FOLLOW_UP' | 'GROUP_SESSION' | 'ASSESSMENT' | 'CRISIS_INTERVENTION';
  status: 'SCHEDULED' | 'CONFIRMED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW' | 'RESCHEDULED';
  description?: string;
  meetingLink?: string;
  location?: string;
  cancellationReason?: string;
  createdAt: string;
  updatedAt: string;
}

export const appointmentService = {
  // Get all appointments (paginated)
  getAllAppointments: async (page = 0, size = 10) => {
    const response = await api.get(`/appointments?page=${page}&size=${size}`);
    return response.data.data;
  },

  // Get appointment by ID
  getAppointmentById: async (id: string): Promise<Appointment> => {
    const response = await api.get(`/appointments/${id}`);
    return response.data.data;
  },

  // Create appointment
  createAppointment: async (appointmentData: {
    studentId: number;
    counselorId: number;
    title: string;
    description?: string;
    appointmentDate: string; // ISO format: "2026-02-01T10:00:00"
    type: 'INITIAL_CONSULTATION' | 'FOLLOW_UP' | 'GROUP_SESSION' | 'ASSESSMENT' | 'CRISIS_INTERVENTION';
  }): Promise<Appointment> => {
    const response = await api.post('/appointments', appointmentData);
    return response.data.data;
  },

  // Update appointment
  updateAppointment: async (id: string, appointmentData: Partial<Appointment>): Promise<Appointment> => {
    const response = await api.put(`/appointments/${id}`, appointmentData);
    return response.data.data;
  },

  // Get appointments by counselor
  getAppointmentsByCounselor: async (counselorId: string, page = 0, size = 10) => {
    const response = await api.get(`/appointments/counselor/${counselorId}?page=${page}&size=${size}`);
    return response.data.data;
  },

  // Get appointments by student
  getAppointmentsByStudent: async (studentId: string, page = 0, size = 10) => {
    const response = await api.get(`/appointments/student/${studentId}?page=${page}&size=${size}`);
    return response.data.data;
  },

  // Get appointments by client
  getAppointmentsByClient: async (clientId: string, page = 0, size = 10) => {
    const response = await api.get(`/appointments/client/${clientId}?page=${page}&size=${size}`);
    return response.data.data;
  },

  // Get upcoming appointments
  getUpcomingAppointments: async (page = 0, size = 10) => {
    const response = await api.get(`/appointments/upcoming?page=${page}&size=${size}`);
    return response.data.data;
  },

  // Get past appointments
  getPastAppointments: async (page = 0, size = 10) => {
    const response = await api.get(`/appointments/past?page=${page}&size=${size}`);
    return response.data.data;
  },

  // Get cancelled appointments
  getCancelledAppointments: async (page = 0, size = 10) => {
    const response = await api.get(`/appointments/cancelled?page=${page}&size=${size}`);
    return response.data.data;
  },

  // Get confirmed appointments
  getConfirmedAppointments: async (page = 0, size = 10) => {
    const response = await api.get(`/appointments/confirmed?page=${page}&size=${size}`);
    return response.data.data;
  },

  // Get pending appointments
  getPendingAppointments: async (page = 0, size = 10) => {
    const response = await api.get(`/appointments/pending?page=${page}&size=${size}`);
    return response.data.data;
  },

  // Get today's appointments
  getTodaysAppointments: async (page = 0, size = 10) => {
    const response = await api.get(`/appointments/today?page=${page}&size=${size}`);
    return response.data.data;
  },

  // Cancel appointment
  cancelAppointment: async (id: string): Promise<Appointment> => {
    const response = await api.put(`/appointments/${id}/cancel`);
    return response.data.data;
  },

  // Confirm appointment
  confirmAppointment: async (id: string): Promise<Appointment> => {
    const response = await api.put(`/appointments/${id}/confirm`);
    return response.data.data;
  },

  // Reschedule appointment
  rescheduleAppointment: async (id: string, newData: {
    appointmentDate: string;
    title?: string;
    description?: string;
  }): Promise<Appointment> => {
    const response = await api.put(`/appointments/${id}/reschedule`, newData);
    return response.data.data;
  },

  // Check counselor availability
  checkAvailability: async (counselorId: string, date: string) => {
    const response = await api.get(
      `/appointments/availability?counselorId=${counselorId}&date=${date}`
    );
    return response.data.data;
  },

  // Get appointment statistics
  getAppointmentStatistics: async () => {
    const response = await api.get('/appointments/stats');
    return response.data.data;
  },

  // Export appointments
  exportAppointments: async (format: 'csv' | 'pdf' = 'csv', startDate?: string, endDate?: string): Promise<Blob> => {
    const params = new URLSearchParams();
    params.append('format', format);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);

    const response = await api.get(`/appointments/export?${params.toString()}`, {
      responseType: 'blob'
    });
    return response.data;
  }
};
```

---

## 6. Session Management Endpoints

**Required Roles:** ADMIN, COUNSELOR

```typescript
// Create src/services/sessionService.ts
import api from './api';

export interface Session {
  id: string;
  clientId: string;
  counselorId: string;
  appointmentId?: string;
  sessionDate: string;
  duration: number;
  sessionType: string;
  notes?: string;
  followUpRequired: boolean;
  followUpDate?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export const sessionService = {
  // Get all sessions (paginated)
  getAllSessions: async (page = 0, size = 10) => {
    const response = await api.get(`/sessions?page=${page}&size=${size}`);
    return response.data.data;
  },

  // Get session by ID
  getSessionById: async (id: string): Promise<Session> => {
    const response = await api.get(`/sessions/${id}`);
    return response.data.data;
  },

  // Create session
  createSession: async (sessionData: {
    appointmentId?: number;
    clientId: number;
    counselorId: number;
    sessionDate: string;
    duration: number;
    sessionType: string;
    notes?: string;
    followUpRequired?: boolean;
    followUpDate?: string;
  }): Promise<Session> => {
    const response = await api.post('/sessions', sessionData);
    return response.data.data;
  },

  // Update session
  updateSession: async (id: string, sessionData: Partial<Session>): Promise<Session> => {
    const response = await api.put(`/sessions/${id}`, sessionData);
    return response.data.data;
  },

  // Delete session
  deleteSession: async (id: string): Promise<void> => {
    await api.delete(`/sessions/${id}`);
  },

  // Get sessions by client
  getSessionsByClient: async (clientId: string, page = 0, size = 10) => {
    const response = await api.get(`/sessions/client/${clientId}?page=${page}&size=${size}`);
    return response.data.data;
  }
};
```

---

## 7. Counselor Endpoints

```typescript
// Create src/services/counselorService.ts
import api from './api';

export interface Counselor {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  specialization?: string;
  qualifications?: string;
  yearsOfExperience?: number;
  licenseNumber?: string;
  availableForAppointments: boolean;
  phoneNumber?: string;
}

export const counselorService = {
  // Get all counselors
  getAllCounselors: async (): Promise<Counselor[]> => {
    const response = await api.get('/counselors');
    return response.data;
  },

  // Get counselor by ID
  getCounselorById: async (id: string): Promise<Counselor> => {
    const response = await api.get(`/counselors/${id}`);
    return response.data;
  }
};
```

---

## 8. Assessment Endpoints

**Required Roles:** STUDENT, CLIENT (submit), COUNSELOR, ADMIN (view all)

```typescript
// Create src/services/assessmentService.ts
import api from './api';

export interface SelfAssessment {
  id: string;
  title: string;
  description?: string;
  questions: Array<{
    id: string;
    text: string;
    type: 'TEXT' | 'MULTIPLE_CHOICE' | 'RATING' | 'BOOLEAN';
    options?: string[];
  }>;
  createdAt: string;
}

export interface RiskAssessment {
  id: string;
  clientId: string;
  assessmentType: string;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  score: number;
  factors: string[];
  recommendations: string[];
  assessedBy: string;
  assessmentDate: string;
  notes?: string;
  createdAt: string;
}

export const assessmentService = {
  // ===== SELF ASSESSMENTS =====
  
  // Get all self assessments
  getAllSelfAssessments: async (): Promise<SelfAssessment[]> => {
    const response = await api.get('/assessments/self');
    return response.data.data;
  },

  // Submit self assessment
  submitSelfAssessment: async (assessmentData: {
    assessmentId: number;
    clientId: number;
    answers: Record<string, any>;
  }) => {
    const response = await api.post('/assessments/self/submit', assessmentData);
    return response.data;
  },

  // ===== RISK ASSESSMENTS =====

  // Create risk assessment
  createRiskAssessment: async (assessmentData: {
    clientId: number;
    assessmentType: string;
    riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
    score: number;
    factors: string[];
    recommendations: string[];
    notes?: string;
  }): Promise<RiskAssessment> => {
    const response = await api.post('/assessments/risk', assessmentData);
    return response.data.data;
  },

  // Get risk assessments for client
  getRiskAssessmentsForClient: async (clientId: string): Promise<RiskAssessment[]> => {
    const response = await api.get(`/assessments/risk/client/${clientId}`);
    return response.data.data;
  }
};
```

---

## 9. Academic Performance Endpoints

**Required Roles:** ADMIN, COUNSELOR (full access), CLIENT (own records only)

```typescript
// Create src/services/academicPerformanceService.ts
import api from './api';

export interface AcademicPerformance {
  id: string;
  clientId: string;
  semester: string;
  year: number;
  gpa: number;
  cgpa?: number;
  academicStanding: string;
  courses: Course[];
  createdAt: string;
  updatedAt: string;
}

export interface Course {
  id: string;
  courseCode: string;
  courseName: string;
  grade: string;
  creditHours: number;
  semester: string;
  year: number;
}

export interface GpaTrendData {
  semester: string;
  year: number;
  gpa: number;
}

export interface StudentAtRiskDto {
  clientId: string;
  studentId: string;
  fullName: string;
  currentGpa: number;
  riskFactors: string[];
}

export const academicPerformanceService = {
  // Create academic performance record
  createRecord: async (data: {
    clientId: number;
    semester: string;
    year: number;
    gpa: number;
    cgpa?: number;
    academicStanding?: string;
    courses: Array<{
      courseCode: string;
      courseName: string;
      grade: string;
      creditHours: number;
    }>;
  }): Promise<AcademicPerformance> => {
    const response = await api.post('/academic-performance', data);
    return response.data.data;
  },

  // Get record by ID
  getRecordById: async (id: string): Promise<AcademicPerformance> => {
    const response = await api.get(`/academic-performance/${id}`);
    return response.data.data;
  },

  // Get records by client ID
  getRecordsByClient: async (clientId: string): Promise<AcademicPerformance[]> => {
    const response = await api.get(`/academic-performance/client/${clientId}`);
    return response.data.data;
  },

  // Get paginated records by client
  getRecordsByClientPaginated: async (clientId: string, page = 0, size = 10) => {
    const response = await api.get(`/academic-performance/client/${clientId}/paginated?page=${page}&size=${size}`);
    return response.data.data;
  },

  // Get latest record for client
  getLatestRecord: async (clientId: string): Promise<AcademicPerformance> => {
    const response = await api.get(`/academic-performance/client/${clientId}/latest`);
    return response.data.data;
  },

  // Get client summary
  getClientSummary: async (clientId: string) => {
    const response = await api.get(`/academic-performance/client/${clientId}/summary`);
    return response.data.data;
  },

  // Get GPA trend
  getGpaTrend: async (clientId: string): Promise<GpaTrendData[]> => {
    const response = await api.get(`/academic-performance/client/${clientId}/gpa-trend`);
    return response.data.data;
  },

  // Update record
  updateRecord: async (id: string, data: Partial<AcademicPerformance>): Promise<AcademicPerformance> => {
    const response = await api.put(`/academic-performance/${id}`, data);
    return response.data.data;
  },

  // Delete record
  deleteRecord: async (id: string): Promise<void> => {
    await api.delete(`/academic-performance/${id}`);
  },

  // Get students at risk
  getStudentsAtRisk: async (): Promise<StudentAtRiskDto[]> => {
    const response = await api.get('/academic-performance/at-risk');
    return response.data.data;
  },

  // Get students with low GPA
  getStudentsWithLowGpa: async (threshold = 2.0): Promise<AcademicPerformance[]> => {
    const response = await api.get(`/academic-performance/low-gpa?threshold=${threshold}`);
    return response.data.data;
  },

  // Get records by faculty
  getByFaculty: async (faculty: string): Promise<AcademicPerformance[]> => {
    const response = await api.get(`/academic-performance/faculty/${faculty}`);
    return response.data.data;
  },

  // Get statistics
  getStatistics: async () => {
    const response = await api.get('/academic-performance/statistics');
    return response.data.data;
  },

  // Get analytics
  getAnalytics: async () => {
    const response = await api.get('/academic-performance/analytics');
    return response.data.data;
  }
};
```

---

## 10. Risk Assessment Endpoints (Full Controller)

**Required Roles:** ADMIN, COUNSELOR

```typescript
// Create src/services/riskAssessmentService.ts
import api from './api';

export const riskAssessmentService = {
  // Get all risk assessments (paginated)
  getAllRiskAssessments: async (page = 0, size = 10) => {
    const response = await api.get(`/risk-assessments?page=${page}&size=${size}`);
    return response.data.data;
  },

  // Get risk assessment by ID
  getRiskAssessmentById: async (id: string): Promise<RiskAssessment> => {
    const response = await api.get(`/risk-assessments/${id}`);
    return response.data.data;
  },

  // Create risk assessment
  createRiskAssessment: async (data: {
    clientId: number;
    assessmentType: string;
    riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
    score: number;
    factors: string[];
    recommendations: string[];
    notes?: string;
  }): Promise<RiskAssessment> => {
    const response = await api.post('/risk-assessments', data);
    return response.data.data;
  },

  // Update risk assessment
  updateRiskAssessment: async (id: string, data: Partial<RiskAssessment>): Promise<RiskAssessment> => {
    const response = await api.put(`/risk-assessments/${id}`, data);
    return response.data.data;
  },

  // Delete risk assessment
  deleteRiskAssessment: async (id: string): Promise<void> => {
    await api.delete(`/risk-assessments/${id}`);
  },

  // Get risk assessments by client
  getRiskAssessmentsByClient: async (clientId: string): Promise<RiskAssessment[]> => {
    const response = await api.get(`/risk-assessments/client/${clientId}`);
    return response.data.data;
  },

  // Get high risk assessments
  getHighRiskAssessments: async (): Promise<RiskAssessment[]> => {
    const response = await api.get('/risk-assessments/high-risk');
    return response.data.data;
  },

  // Get risk assessment statistics
  getStats: async () => {
    const response = await api.get('/risk-assessments/stats');
    return response.data.data;
  },

  // Escalate risk assessment
  escalateRiskAssessment: async (id: string): Promise<RiskAssessment> => {
    const response = await api.post(`/risk-assessments/${id}/escalate`);
    return response.data.data;
  },

  // Export risk assessments
  exportRiskAssessments: async (format: 'csv' | 'pdf' = 'csv'): Promise<Blob> => {
    const response = await api.get(`/risk-assessments/export?format=${format}`, {
      responseType: 'blob'
    });
    return response.data;
  },

  // Get latest risk assessment for client
  getLatestForClient: async (clientId: string): Promise<RiskAssessment> => {
    const response = await api.get(`/risk-assessments/client/${clientId}/latest`);
    return response.data.data;
  },

  // Get risk assessment trend
  getTrend: async (clientId: string) => {
    const response = await api.get(`/risk-assessments/client/${clientId}/trend`);
    return response.data.data;
  },

  // Get risk assessment summary
  getSummary: async () => {
    const response = await api.get('/risk-assessments/summary');
    return response.data.data;
  },

  // Get assessments requiring follow-up
  getFollowUpRequired: async (): Promise<RiskAssessment[]> => {
    const response = await api.get('/risk-assessments/follow-up-required');
    return response.data.data;
  },

  // Get assessments by assessor
  getByAssessor: async (assessorId: string) => {
    const response = await api.get(`/risk-assessments/assessor?assessorId=${assessorId}`);
    return response.data.data;
  },

  // Get analytics
  getAnalytics: async () => {
    const response = await api.get('/risk-assessments/analytics');
    return response.data.data;
  }
};
```

---

## 11. Dashboard & Analytics Endpoints

### Dashboard Service (Update existing)

```typescript
// Update src/services/dashboardService.ts
import api from './api';
import { DashboardStats } from '../types/api';

export const dashboardService = {
  // Get dashboard statistics
  getStats: async (): Promise<DashboardStats> => {
    const response = await api.get('/dashboard/stats');
    return response.data.data;
  },

  // Get recent appointments
  getRecentAppointments: async () => {
    const response = await api.get('/dashboard/recent-appointments');
    return response.data;
  },

  // Get upcoming appointments
  getUpcomingAppointments: async () => {
    const response = await api.get('/dashboard/upcoming-appointments');
    return response.data;
  },

  // Get at-risk students
  getAtRiskStudents: async () => {
    const response = await api.get('/dashboard/at-risk-students');
    return response.data;
  }
};
```

### Analytics Service (Create new)

```typescript
// Create src/services/analyticsService.ts
import api from './api';

export interface InterventionReport {
  totalCases: number;
  highPriorityCases: number;
  mediumPriorityCases: number;
  lowPriorityCases: number;
  interventionsRequired: Array<{
    clientId: string;
    clientName: string;
    reason: string;
    priority: 'HIGH' | 'MEDIUM' | 'LOW';
    recommendedAction: string;
  }>;
}

export const analyticsService = {
  // Get analytics overview
  getOverview: async (): Promise<DashboardStats> => {
    const response = await api.get('/analytics/overview');
    return response.data.data;
  },

  // Get intervention