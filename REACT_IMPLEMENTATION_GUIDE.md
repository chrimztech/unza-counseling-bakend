# UNZA Counseling System - Complete React Implementation Guide

## Executive Summary
This guide provides complete TypeScript code for integrating your React frontend with the UNZA Counseling Spring Boot backend. **Deadline: Tuesday**.

---

## Section 1: Complete Service Layer Implementation

### 1.1 User Service (`src/services/userService.ts`)
```typescript
import api from './api';
import { PaginatedResponse, User, RegisterRequest } from '../types/api';

export const userService = {
  getAllUsers: async (page = 0, size = 10): Promise<PaginatedResponse<User>> => {
    const response = await api.get(`/users?page=${page}&size=${size}`);
    return response.data.data;
  },

  getUserById: async (id: string): Promise<User> => {
    const response = await api.get(`/users/${id}`);
    return response.data.data;
  },

  getUserByEmail: async (email: string): Promise<User> => {
    const response = await api.get(`/users/email/${email}`);
    return response.data.data;
  },

  createUser: async (userData: RegisterRequest): Promise<User> => {
    const response = await api.post('/users', userData);
    return response.data.data;
  },

  updateUser: async (id: string, userData: Partial<User>): Promise<User> => {
    const response = await api.put(`/users/${id}`, userData);
    return response.data.data;
  },

  deleteUser: async (id: string): Promise<void> => {
    await api.delete(`/users/${id}`);
  },

  getUsersByRole: async (role: string): Promise<User[]> => {
    const response = await api.get(`/users/role/${role}`);
    return response.data.data;
  },

  searchUsers: async (query: string, page = 0, size = 10): Promise<PaginatedResponse<User>> => {
    const response = await api.get(`/users/search?query=${query}&page=${page}&size=${size}`);
    return response.data.data;
  },

  getActiveUsers: async (page = 0, size = 10): Promise<PaginatedResponse<User>> => {
    const response = await api.get(`/users/active?page=${page}&size=${size}`);
    return response.data.data;
  },

  getInactiveUsers: async (page = 0, size = 10): Promise<PaginatedResponse<User>> => {
    const response = await api.get(`/users/inactive?page=${page}&size=${size}`);
    return response.data.data;
  },

  activateUser: async (id: string): Promise<User> => {
    const response = await api.put(`/users/${id}/activate`);
    return response.data.data;
  },

  deactivateUser: async (id: string): Promise<User> => {
    const response = await api.put(`/users/${id}/deactivate`);
    return response.data.data;
  },

  getCurrentProfile: async (): Promise<User> => {
    const response = await api.get('/users/profile');
    return response.data.data;
  },

  getAllRoles: async (): Promise<string[]> => {
    const response = await api.get('/users/roles');
    return response.data.data;
  },

  getUserCountByRole: async (): Promise<Record<string, number>> => {
    const response = await api.get('/users/count-by-role');
    return response.data.data;
  },

  changeUserPassword: async (id: string, newPassword: string): Promise<void> => {
    await api.put(`/users/${id}/password?newPassword=${newPassword}`);
  },

  exportUsers: async (format: 'csv' | 'pdf' = 'csv'): Promise<Blob> => {
    const response = await api.get(`/users/export?format=${format}`, { responseType: 'blob' });
    return response.data;
  }
};
```

### 1.2 Enhanced Client Service
```typescript
import api from './api';
import { Client, PaginatedResponse } from '../types/api';

export const clientService = {
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
    return response.data;
  },

  getClient: async (id: string): Promise<Client> => {
    const response = await api.get(`/clients/${id}`);
    return response.data;
  },

  getClientByStudentId: async (studentId: string): Promise<Client> => {
    const response = await api.get(`/clients/student/${studentId}`);
    return response.data;
  },

  updateClient: async (id: string, clientData: Partial<Client>): Promise<Client> => {
    const response = await api.put(`/clients/${id}`, clientData);
    return response.data;
  },

  getClientStats: async (): Promise<{ activeClients: number; highRiskClients: number }> => {
    const response = await api.get('/clients/stats');
    return response.data.data;
  },

  updateRiskLevel: async (id: string, riskLevel: 'LOW' | 'MODERATE' | 'HIGH' | 'CRITICAL'): Promise<Client> => {
    const response = await api.put(`/clients/${id}/risk-level?riskLevel=${riskLevel}`);
    return response.data;
  }
};
```

### 1.3 Complete Appointment Service
```typescript
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
  getAllAppointments: async (page = 0, size = 10) => {
    const response = await api.get(`/appointments?page=${page}&size=${size}`);
    return response.data.data;
  },

  getAppointmentById: async (id: string): Promise<Appointment> => {
    const response = await api.get(`/appointments/${id}`);
    return response.data.data;
  },

  createAppointment: async (appointmentData: {
    studentId: number;
    counselorId: number;
    title: string;
    description?: string;
    appointmentDate: string;
    type: 'INITIAL_CONSULTATION' | 'FOLLOW_UP' | 'GROUP_SESSION' | 'ASSESSMENT' | 'CRISIS_INTERVENTION';
  }): Promise<Appointment> => {
    const response = await api.post('/appointments', appointmentData);
    return response.data.data;
  },

  updateAppointment: async (id: string, appointmentData: Partial<Appointment>): Promise<Appointment> => {
    const response = await api.put(`/appointments/${id}`, appointmentData);
    return response.data.data;
  },

  getAppointmentsByCounselor: async (counselorId: string, page = 0, size = 10) => {
    const response = await api.get(`/appointments/counselor/${counselorId}?page=${page}&size=${size}`);
    return response.data.data;
  },

  getAppointmentsByStudent: async (studentId: string, page = 0, size = 10) => {
    const response = await api.get(`/appointments/student/${studentId}?page=${page}&size=${size}`);
    return response.data.data;
  },

  getAppointmentsByClient: async (clientId: string, page = 0, size = 10) => {
    const response = await api.get(`/appointments/client/${clientId}?page=${page}&size=${size}`);
    return response.data.data;
  },

  getUpcomingAppointments: async (page = 0, size = 10) => {
    const response = await api.get(`/appointments/upcoming?page=${page}&size=${size}`);
    return response.data.data;
  },

  getPastAppointments: async (page = 0, size = 10) => {
    const response = await api.get(`/appointments/past?page=${page}&size=${size}`);
    return response.data.data;
  },

  getCancelledAppointments: async (page = 0, size = 10) => {
    const response = await api.get(`/appointments/cancelled?page=${page}&size=${size}`);
    return response.data.data;
  },

  getConfirmedAppointments: async (page = 0, size = 10) => {
    const response = await api.get(`/appointments/confirmed?page=${page}&size=${size}`);
    return response.data.data;
  },

  getPendingAppointments: async (page = 0, size = 10) => {
    const response = await api.get(`/appointments/pending?page=${page}&size=${size}`);
    return response.data.data;
  },

  getTodaysAppointments: async (page = 0, size = 10) => {
    const response = await api.get(`/appointments/today?page=${page}&size=${size}`);
    return response.data.data;
  },

  cancelAppointment: async (id: string): Promise<Appointment> => {
    const response = await api.put(`/appointments/${id}/cancel`);
    return response.data.data;
  },

  confirmAppointment: async (id: string): Promise<Appointment> => {
    const response = await api.put(`/appointments/${id}/confirm`);
    return response.data.data;
  },

  rescheduleAppointment: async (id: string, newData: { appointmentDate: string; title?: string; description?: string }): Promise<Appointment> => {
    const response = await api.put(`/appointments/${id}/reschedule`, newData);
    return response.data.data;
  },

  checkAvailability: async (counselorId: string, date: string) => {
    const response = await api.get(`/appointments/availability?counselorId=${counselorId}&date=${date}`);
    return response.data.data;
  },

  getAppointmentStatistics: async () => {
    const response = await api.get('/appointments/stats');
    return response.data.data;
  },

  exportAppointments: async (format: 'csv' | 'pdf' = 'csv', startDate?: string, endDate?: string): Promise<Blob> => {
    const params = new URLSearchParams();
    params.append('format', format);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    const response = await api.get(`/appointments/export?${params.toString()}`, { responseType: 'blob' });
    return response.data;
  }
};
```

### 1.4 Session Service
```typescript
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
  getAllSessions: async (page = 0, size = 10) => {
    const response = await api.get(`/sessions?page=${page}&size=${size}`);
    return response.data.data;
  },

  getSessionById: async (id: string): Promise<Session> => {
    const response = await api.get(`/sessions/${id}`);
    return response.data.data;
  },

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

  updateSession: async (id: string, sessionData: Partial<Session>): Promise<Session> => {
    const response = await api.put(`/sessions/${id}`, sessionData);
    return response.data.data;
  },

  deleteSession: async (id: string): Promise<void> => {
    await api.delete(`/sessions/${id}`);
  },

  getSessionsByClient: async (clientId: string, page = 0, size = 10) => {
    const response = await api.get(`/sessions/client/${clientId}?page=${page}&size=${size}`);
    return response.data.data;
  }
};
```

### 1.5 Notification Service
```typescript
import api from './api';

export interface Notification {
  id: string;
  userId: string;
  title: string;
  message: string;
  type: 'APPOINTMENT' | 'REMINDER' | 'ALERT' | 'MESSAGE' | 'SYSTEM';
  read: boolean;
  createdAt: string;
}

export const notificationService = {
  getUserNotifications: async (): Promise<Notification[]> => {
    const response = await api.get('/notifications');
    return response.data;
  },

  markAsRead: async (id: string): Promise<void> => {
    await api.put(`/notifications/${id}/read`);
  },

  markAllAsRead: async (): Promise<void> => {
    await api.put('/notifications/read-all');
  },

  getUnreadCount: async (): Promise<number> => {
    const response = await api.get('/notifications/unread-count');
    return response.data.count;
  }
};
```

### 1.6 Message Service
```typescript
import api from './api';

export interface Message {
  id: string;
  senderId: string;
  recipientId: string;
  subject: string;
  content: string;
  read: boolean;
  delivered: boolean;
  createdAt: string;
  readAt?: string;
}

export interface MessageRequest {
  recipientId: number;
  subject: string;
  content: string;
}

export const messageService = {
  sendMessage: async (request: MessageRequest): Promise<Message> => {
    const response = await api.post('/messages', request);
    return response.data;
  },

  getMessages: async (): Promise<Message[]> => {
    const response = await api.get('/messages');
    return response.data;
  },

  getMessageById: async (id: string): Promise<Message> => {
    const response = await api.get(`/messages/${id}`);
    return response.data;
  },

  updateMessage: async (id: string, request: Partial<MessageRequest>): Promise<Message> => {
    const response = await api.put(`/messages/${id}`, request);
    return response.data;
  },

  deleteMessage: async (id: string): Promise<void> => {
    await api.delete(`/messages/${id}`);
  },

  getMessagesByConversation: async (conversationId: string): Promise<Message[]> => {
    const response = await api.get(`/messages/conversation/${conversationId}`);
    return response.data;
  },

  getMessagesByUser: async (userId: string): Promise<Message[]> => {
    const response = await api.get(`/messages/user/${userId}`);
    return response.data;
  },

  searchMessages: async (query: string): Promise<Message[]> => {
    const response = await api.get(`/messages/search?query=${query}`);
    return response.data;
  },

  markMessageAsRead: async (id: string): Promise<void> => {
    await api.put(`/messages/${id}/read`);
  },

  markMessageAsDelivered: async (id: string): Promise<void> => {
    await api.put(`/messages/${id}/delivered`);
  }
};
```

### 1.7 Settings Service
```typescript
import api from './api';

export interface Settings {
  [key: string]: any;
}

export const settingsService = {
  getAllSettings: async (): Promise<Settings> => {
    const response = await api.get('/settings');
    return response.data.data;
  },

  getSettingsByCategory: async (category: string): Promise<Settings> => {
    const response = await api.get(`/settings/${category}`);
    return response.data.data;
  },

  updateSetting: async (key: string, value: any): Promise<any> => {
    const response = await api.put(`/settings/${key}`, { value });
    return response.data.data;
  },

  createSetting: async (settingData: { key: string; value: any; category: string }): Promise<any> => {
    const response = await api.post('/settings', settingData);
    return response.data.data;
  },

  deleteSetting: async (key: string): Promise<void> => {
    await api.delete(`/settings/${key}`);
  },

  getOrganizationSettings: async (): Promise<Settings> => {
    const response = await api.get('/settings/organization');
    return response.data.data;
  },

  getAppointmentSettings: async (): Promise<Settings> => {
    const response = await api.get('/settings/appointments');
    return response.data.data;
  },

  getNotificationSettings: async (): Promise<Settings> => {
    const response = await api.get('/settings/notifications');
    return response.data.data;
  },

  getSecuritySettings: async (): Promise<Settings> => {
    const response = await api.get('/settings/security');
    return response.data.data;
  },

  updateOrganizationSetting: async (key: string, value: any): Promise<any> => {
    const response = await api.put(`/settings/organization/${key}`, { value });
    return response.data.data;
  },

  updateAppointmentSetting: async (key: string, value: any): Promise<any> => {
    const response = await api.put(`/settings/appointments/${key}`, { value });
    return response.data.data;
  },

  updateNotificationSetting: async (key: string, value: any): Promise<any> => {
    const response = await api.put(`/settings/notifications/${key}`, { value });
    return response.data.data;
  },

  updateSecuritySetting: async (key: string, value: any): Promise<any> => {
    const response = await api.put(`/settings/security/${key}`, { value });
    return response.data.data;
  }
};
```

### 1.8 Consent Form Service
```typescript
import api from './api';

export interface ConsentForm {
  id: string;
  title: string;
  content: string;
  version: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UserConsent {
  id: string;
  userId: string;
  consentFormId: string;
  signedAt: string;
  ipAddress?: string;
}

export const consentFormService = {
  createConsentForm: async (data: { title: string; content: string; version: string }): Promise<ConsentForm> => {
    const response = await api.post('/consent/forms', data);
    return response.data;
  },

  updateConsentForm: async (id: string, data: Partial<ConsentForm>): Promise<ConsentForm> => {
    const response = await api.put(`/consent/forms/${id}`, data);
    return response.data;
  },

  getConsentForm: async (id: string): Promise<ConsentForm> => {
    const response = await api.get(`/consent/forms/${id}`);
    return response.data;
  },

  getAllConsentForms: async (): Promise<ConsentForm[]> => {
    const response = await api.get('/consent/forms');
    return response.data;
  },

  getActiveConsentForms: async (): Promise<ConsentForm[]> => {
    const response = await api.get('/consent/forms/active');
    return response.data;
  },

  getLatestActiveConsentForm: async (): Promise<ConsentForm> => {
    const response = await api.get('/consent/forms/latest');
    return response.data;
  },

  signConsentForm: async (data: { consentFormId: number; agreed: boolean }): Promise<UserConsent> => {
    const response = await api.post('/consent/sign', data);
    return response.data;
  },

  hasUserSignedLatestConsent: async (): Promise<boolean> => {
    const response = await api.get('/consent/check-signed');
    return response.data;
  },

  getUserConsentHistory: async (): Promise<UserConsent[]> => {
    const response = await api.get('/consent/history');
    return response.data;
  },

  getConsentStatistics: async () => {
    const response = await api.get('/consent/statistics');
    return response.data;
  },

  deleteConsentForm: async (id: string): Promise<void> => {
    await api.delete(`/consent/forms/${id}`);
  },

  activateConsentForm: async (id: string): Promise<void> => {
    await api.post(`/consent/forms/${id}/activate`);
  },

  deactivateConsentForm: async (id: string): Promise<void> => {
    await api.post(`/consent/forms/${id}/deactivate`);
  }
};
```

### 1.9 Report Service
```typescript
import api from './api';

export interface Report {
  id: string;
  reportType: string;
  title: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  status: 'PENDING' | 'GENERATING' | 'COMPLETED' | 'FAILED';
  fileUrl?: string;
  createdBy: string;
  createdAt: string;
  completedAt?: string;
}

export const reportService = {
  getAllReports: async (page = 0, size = 10) => {
    const response = await api.get(`/reports?page=${page}&size=${size}`);
    return response.data.data;
  },

  getReportById: async (id: string): Promise<Report> => {
    const response = await api.get(`/reports/${id}`);
    return response.data.data;
  },

  generateReport: async (reportType: string, startDate?: string, endDate?: string): Promise<Report> => {
    const params = new URLSearchParams();
    params.append('reportType', reportType);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    const response = await api.post(`/reports?${params.toString()}`);
    return response.data.data;
  },

  updateReport: async (id: string, reportData: Partial<Report>): Promise<Report> => {
    const response = await api.put(`/reports/${id}`, reportData);
    return response.data.data;
  },

  deleteReport: async (id: string): Promise<void> => {
    await api.delete(`/reports/${id}`);
  },

  getReportTypes: async (): Promise<string[]> => {
    const response = await api.get('/reports/types');
    return response.data.data;
  },

  scheduleReport: async (reportType: string, schedule: string, startDate?: string, endDate?: string): Promise<void> => {
    const params = new URLSearchParams();
    params.append('reportType', reportType);
    params.append('schedule', schedule);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    await api.post(`/reports/schedule?${params.toString()}`);
  },

  exportReport: async (id: string, format: 'pdf' | 'csv' | 'excel' = 'pdf'): Promise<Blob> => {
    const response = await api.get(`/reports/export/${id}?format=${format}`, { responseType: 'blob' });
    return response.data;
  },

  getReportHistory: async (page = 0, size = 10) => {
    const response = await api.get(`/reports/history?page=${page}&size=${size}`);
    return response.data.data;
  },

  getScheduledReports: async (): Promise<Report[]> => {
    const response = await api.get('/reports/scheduled');
    return response.data.data;
  },

  updateReportSchedule: async (scheduleId: string, schedule: string, startDate?: string, endDate?: string): Promise<void> => {
    const params = new URLSearchParams();
    params.append('schedule', schedule);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    await api.put(`/reports/schedule/${scheduleId}?${params.toString()}`);
  },

  deleteReportSchedule: async (scheduleId: string): Promise<void> => {
    await api.delete(`/reports/schedule/${scheduleId}`);
  },

  getReportStatistics: async () => {
    const response = await api.get('/reports/statistics');
    return response.data.data;
  },

  getReportAnalytics: async () => {
    const response = await api.get('/reports/analytics');
    return response.data.data;
  },

  duplicateReport: async (id: string): Promise<Report> => {
    const response = await api.post(`/reports/${id}/duplicate`);
    return response.data.data;
  },

  archiveReport: async (id: string): Promise<void> => {
    await api.post(`/reports/${id}/archive`);
  },

  restoreReport: async (id: string): Promise<void> => {
    await api.post(`/reports/${id}/restore`);
  },

  getArchivedReports: async (): Promise<Report[]> => {
    const response = await api.get('/reports/archived');
    return response.data.data;
  },

  getReportSummary: async () => {
    const response = await api.get('/reports/summary');
    return response.data.data;
  },

  getAppointmentTrends: async () => {
    const response = await api.get('/reports/appointment-trends');
    return response.data.data;
  },

  getPresentingConcerns: async () => {
    const response = await api.get('/reports/presenting-concerns');
    return response.data.data;
  },

  getRecentSessions: async () => {
    const response = await api.get('/reports/recent-sessions');
    return response.data.data;
  },

  getAllReportData: async () => {
    const response = await api.get('/reports/all');
    return response.data.data;
  }
};
```

---

## Section 2: TypeScript Types (`src/types/api.ts`)

Update your types file with these complete definitions:

```typescript
// User Types
export interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  studentId?: string;
  phoneNumber?: string;
  profilePicture?: string;
  bio?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER' | 'PREFER_NOT_TO_SAY';
  dateOfBirth?: string;
  department?: string;
  program?: string;
  yearOfStudy?: number;
  roles: { name: string }[];
  active: boolean;
  emailVerified: boolean;
  lastLogin?: string;
  licenseNumber?: string;
  specialization?: string;
  qualifications?: string;
  yearsOfExperience?: number;
  availableForAppointments?: boolean;
  hasSignedConsent?: boolean;
  createdAt: string;
  updatedAt: string;
}

// Client Types
export interface Client {
  id: string;
  user: User;
  studentId?: string;
  programme?: string;
  faculty?: string;
  yearOfStudy?: number;
  gpa?: number;
  clientStatus: 'ACTIVE' | 'INACTIVE' | 'COMPLETED' | 'REFERRED' | 'ON_HOLD' | 'WITHDRAWN';
  riskLevel: 'LOW' | 'MODERATE' | 'HIGH' | 'CRITICAL';
  riskScore: number;
  totalSessions: number;
  registrationDate?: string;
  lastSessionDate?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  emergencyContactRelationship?: string;
  medicalHistory?: string;
  counselingHistory?: string;
  referralSource?: string;
  consentToTreatment: boolean;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

// Auth Types
export interface LoginRequest {
  identifier: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role?: string;
  phoneNumber?: string;
  studentId?: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  user: User;
  expiresIn: number;
}

// Appointment Types
export interface Appointment {
  id: string;
  title: string;
  student: User;
  counselor: User;
  appointmentDate: string;
  duration: number;
  type: 'INITIAL_CONSULTATION' | 'FOLLOW_UP' | 'GROUP_SESSION' | 'ASSESSMENT' | 'CRISIS_INTERVENTION';
  status: 'SCHEDULED' | 'CONFIRMED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW' | 'RESCHEDULED';
  description?: string;
  meetingLink?: string;
  location?: string;
  cancellationReason?: string;
  reminderSent: boolean;
  createdAt: string;
  updatedAt: string;
}

// Session Types
export interface Session {
  id: string;
  appointment?: Appointment;
  client: Client;
  counselor: User;
  sessionDate: string;
  duration: number;
  sessionType: string;
  notes?: string;
  followUpRequired: boolean;
  followUpDate?: string;
  createdAt: string;
  updatedAt: string;
}

// Academic Performance Types
export interface Course {
  id: string;
  courseCode: string;
  courseName: string;
  grade: string;
  creditHours: number;
  semester: string;
  year: number;
}

export interface AcademicPerformance {
  id: string;
  client: Client;
  semester: string;
  year: number;
  gpa: number;
  cgpa?: number;
  academicStanding: string;
  courses: Course[];
  createdAt: string;
  updatedAt: string;
}

// Risk Assessment Types
export interface RiskAssessment {
  id: string;
  client: Client;
  assessmentType: string;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  score: number;
  factors: string[];
  recommendations: string[];
  assessedBy: User;
  assessmentDate: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

// Self Assessment Types
export interface SelfAssessment {
  id: string;
  title: string;
  description?: string;
  questions: AssessmentQuestion[];
  createdAt: string;
}

export interface AssessmentQuestion {
  id: string;
  text: string;
  type: 'TEXT' | 'MULTIPLE_CHOICE' | 'RATING' | 'BOOLEAN';
  options?: string[];
  required: boolean;
}

// Dashboard Types
export interface DashboardStats {
  totalClients: number;
  totalAppointments: number;
  upcomingAppointments: number;
  completedAppointments: number;
  atRiskStudents: number;
  totalSessions: number;
  newClientsThisMonth: number;
  appointmentsThisWeek: number;
}

// API Response Types
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  errors?: string[];
  timestamp?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  number: number;
  size: number;
  numberOfElements: number;
  empty: boolean;
}

// Notification Types
export interface Notification {
  id: string;
  userId: string;
  title: string;
  message: string;
  type: 'APPOINTMENT' | 'REMINDER' | 'ALERT' | 'MESSAGE' | 'SYSTEM';
  read: boolean;
  createdAt: string;
}

// Message Types
export interface Message {
  id: string;
  sender: User;
  recipient: User;
  subject: string;
  content: string;
  read: boolean;
  delivered: boolean;
  createdAt: string;
  readAt?: string;
}

// Counselor Types
export interface Counselor {
  id: string;
  user: User;
  specialization?: string;
  qualifications?: string;
  yearsOfExperience?: number;
  licenseNumber?: string;
  availableForAppointments: boolean;
}

// Resource Types
export interface Resource {
  id: string;
  title: string;
  description?: string;
  type: 'ARTICLE' | 'VIDEO' | 'DOCUMENT' | 'LINK';
  category: string;
  url?: string;
  content?: string;
  featured: boolean;
  createdAt: string;
  updatedAt: string;
}
```

---

## Section 3: Role-Based Access Control

### Auth Guard Hook
```typescript
// src/hooks/useAuth.ts
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import { User } from '../types/api';

export const useAuth = () => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const initAuth = async () => {
      if (!authService.isAuthenticated()) {
        navigate('/login');
        return;
      }

      try {
        const userData = await authService.getProfile();
        setUser(userData);
      } catch (error) {
        authService.removeToken();
        navigate('/login');
      } finally {
        setLoading(false);
      }
    };

    initAuth();
  }, [navigate]);

  const hasRole = (role: string): boolean => {
    if (!user) return false;
    return user.roles.some(r => r.name === role || r.name === `ROLE_${role}`);
  };

  const hasAnyRole = (roles: string[]): boolean => {
    return roles.some(role => hasRole(role));
  };

  return { user, loading, hasRole, hasAnyRole };
};
```

### Protected Route Component
```typescript
// src/components/ProtectedRoute.tsx
import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRoles?: string[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredRoles }) => {
  const { user, loading, hasAnyRole } = useAuth();

  if (loading) {
    return <div>Loading...</div>;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRoles && !hasAnyRole(requiredRoles)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <>{children}</>;
};
```

---

## Section 4: Essential React Components

### Dashboard Component
```typescript
// src/components/Dashboard.tsx
import React, { useEffect, useState } from 'react';
import { dashboardService } from '../services/dashboardService';
import { DashboardStats } from '../types/api';

export const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      const data = await dashboardService.getStats();
      setStats(data);
    } catch (error) {
      console.error('Failed to load dashboard stats:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="dashboard">
      <h1>Dashboard</h1>
      <div className="stats-grid">
        <StatCard title="Total Clients" value={stats?.totalClients || 0} />
        <StatCard title="Total Appointments" value={stats?.totalAppointments || 0} />
        <StatCard title="Upcoming" value={stats?.upcomingAppointments || 0} />
        <StatCard title="At Risk Students" value={stats?.atRiskStudents || 0} />
      </div>
    </div>
  );
};

const StatCard: React.FC<{ title: string; value: number }> = ({ title, value }) => (
  <div className="stat-card">
    <h3>{title}</h3>
    <p className="stat-value">{value}</p>
  </div>
);
```

### Client List Component
```typescript
// src/components/ClientList.tsx
import React, { useEffect, useState } from 'react';
import { clientService } from '../services/clientService';
import { Client, PaginatedResponse } from '../types/api';

export const ClientList: React.FC = () => {
  const [clients, setClients] = useState<PaginatedResponse<Client> | null>(null);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');

  useEffect(() => {
    loadClients();
  }, [page, search]);

  const loadClients = async () => {
    try {
      const data = await clientService.getClients({
        page,
        size: 10,
        search: search || undefined
      });
      setClients(data);
    } catch (error) {
      console.error('Failed to load clients:', error);
    }
  };

  return (
    <div className="client-list">
      <h2>Clients</h2>
      <input
        type="text"
        placeholder="Search clients..."
        value={search}
        onChange={(e) => setSearch(e.target.value)}
      />
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Student ID</th>
            <th>Risk Level</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {clients?.content.map(client => (
            <tr key={client.id}>
              <td>{client.user.firstName} {client.user.lastName}</td>
              <td>{client.user.email}</td>
              <td>{client.studentId}</td>
              <td>{client.riskLevel}</td>
              <td>{client.clientStatus}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="pagination">
        <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}>
          Previous
        </button>
        <span>Page {page + 1} of {clients?.totalPages}</span>
        <button onClick={() => setPage(p => p + 1)} disabled={clients?.last}>
          Next
        </button>
      </div>
    </div>
  );
};
```

---

## Section 5: Complete Service Exports

Create `src/services/index.ts`:
```typescript
export { api, endpoints } from './api';
export { authService } from './authService';
export { userService } from './userService';
export { clientService } from './clientService';
export { appointmentService } from './appointmentService';
export { sessionService } from './sessionService';
export { counselorService } from './counselorService';
export { assessmentService } from './assessmentService';
export { academicPerformanceService } from './academicPerformanceService';
export { riskAssessmentService } from './riskAssessmentService';
export { dashboardService } from './dashboardService';
export { analyticsService } from './analyticsService';
export { reportService } from './reportService';
export { notificationService } from './notificationService';
export { messageService } from './messageService';
export { settingsService } from './settingsService';
export { consentFormService } from './consentFormService';
export { resourceService } from './resourceService';
```

---

## Section 6: Error Handling Pattern

```typescript
// src/utils/errorHandler.ts
export const handleApiError = (error: any): string => {
  if (error.response) {
    const { status, data } = error.response;
    
    switch (status) {
      case 400:
        return data.message || 'Bad request. Please check your input.';
      case 401:
        return 'Session expired. Please login again.';
      case 403:
        return 'You do not have permission to perform this action.';
      case 404:
        return 'The requested resource was not found.';
      case 409:
        return data.message || 'A conflict occurred with the current state.';
      case 422:
        return data.message || 'Validation failed. Please check your input.';
      case 500:
        return 'Server error. Please try again later.';
      default:
        return data.message || 'An unexpected error occurred.';
    }
  }
  
  if (error.request) {
    return 'Network error. Please check your connection.';
  }
  
  return 'An unexpected error occurred.';
};
```

---

## Section 7: Quick Implementation Checklist

### Priority 1 (Critical - Must Have by Tuesday)
- [ ] Authentication (login/logout/refresh)
- [ ] Dashboard with statistics
- [ ] Client list and detail view
- [ ] Appointment scheduling
- [ ] Session notes

### Priority 2 (Important)
- [ ] User management (Admin)
- [ ] Risk assessments
- [ ] Notifications
- [ ] Academic performance tracking
- [ ] Reports

### Priority 3 (Nice to Have)
- [ ] Messaging system
- [ ] Settings management
- [ ] Consent forms
- [ ] Analytics dashboards
- [ ] Export functionality

---

## Section 8: API Testing Commands

Test your backend endpoints with curl:

```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"admin@unza.zm","password":"Admin@123"}'

# Get dashboard stats
curl -X GET http://localhost:8080/api/v1/dashboard/stats \
  -H "Authorization: Bearer YOUR_TOKEN"

# Get clients
curl -X GET http://localhost:8080/api/v1/clients?page=0&size=10 \
  -H "Authorization: Bearer YOUR_TOKEN"

# Create appointment
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "counselorId": 2,
    "title": "Initial Consultation",
    "appointmentDate": "2026-02-05T10:00:00",
    "type": "INITIAL_CONSULTATION"
  }'
```

---

## Section 9: Backend Endpoints Reference

### Authentication Endpoints
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/login` | Login user | No |
| POST | `/auth/register` | Register new user | No |
| POST | `/auth/logout` | Logout user | Yes |
| POST | `/auth/refresh` | Refresh token | Yes |
| POST | `/auth/anonymous-login` | Anonymous login | No |
| POST | `/auth/password-reset-request` | Request password reset | No |
| POST | `/auth/password-reset` | Reset password | No |
| GET | `/auth/validate-token` | Validate token | Yes |

### User Endpoints
| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| GET | `/users` | Get all users | ADMIN, COUNSELOR |
| GET | `/users/{id}` | Get user by ID | ADMIN, COUNSELOR |
| POST | `/users` | Create user | ADMIN |
| PUT | `/users/{id}` | Update user | ADMIN |
| DELETE | `/users/{id}` | Delete user | ADMIN |
| GET | `/users/profile` | Get current profile | Any |

### Client Endpoints
| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| GET | `/clients` | Get all clients | ADMIN, COUNSELOR |
| GET | `/clients/{id}` | Get client by ID | ADMIN, COUNSELOR, Own |
| PUT | `/clients/{id}` | Update client | ADMIN, Own |
| GET | `/clients/stats` | Get client stats | ADMIN, COUNSELOR |
| PUT | `/clients/{id}/risk-level` | Update risk level | ADMIN, COUNSELOR |

### Appointment Endpoints
| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| GET | `/appointments` | Get all appointments | Any |
| GET | `/appointments/{id}` | Get appointment | Any |
| POST | `/appointments` | Create appointment | Any |
| PUT | `/appointments/{id}` | Update appointment | Any |
| PUT | `/appointments/{id}/cancel` | Cancel appointment | Any |
| PUT | `/appointments/{id}/confirm` | Confirm appointment | ADMIN, COUNSELOR |
| PUT | `/appointments/{id}/reschedule` | Reschedule | Any |
| GET | `/appointments/upcoming` | Get upcoming | Any |
| GET | `/appointments/today` | Get today's | Any |

### Session Endpoints
| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| GET | `/sessions` | Get all sessions | ADMIN, COUNSELOR |
| GET | `/sessions/{id}` | Get session | ADMIN, COUNSELOR |
| POST | `/sessions` | Create session | ADMIN, COUNSELOR |
| PUT | `/sessions/{id}` | Update session | ADMIN, COUNSELOR |
| DELETE | `/sessions/{id}` | Delete session | ADMIN |
| GET | `/sessions/client/{id}` | Get by client | ADMIN, COUNSELOR, Own |

### Assessment Endpoints
| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| GET | `/assessments/self` | Get self assessments | Any |
| POST | `/assessments/self/submit` | Submit assessment | STUDENT, CLIENT |
| POST | `/assessments/risk` | Create risk assessment | ADMIN, COUNSELOR |
| GET | `/assessments/risk/client/{id}` | Get client risk assessments | ADMIN, COUNSELOR |

---

**END OF INTEGRATION GUIDE**

For questions or issues:
1. Check browser console for errors
2. Verify backend is running on port 8080
3. Check network tab for API responses
4. Ensure JWT token is being sent in Authorization header
