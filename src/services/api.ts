import axios from 'axios';

// API Configuration
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/v1';

// Create axios instance
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
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized access
      localStorage.removeItem('authToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;

// API Endpoints
export const endpoints = {
  // Auth
  auth: {
    login: '/auth/login',
    register: '/auth/register',
    logout: '/auth/logout',
    refresh: '/auth/refresh',
    profile: '/auth/profile',
  },
  
  // Users
  users: {
    list: '/users',
    get: (id: string) => `/users/${id}`,
    create: '/users',
    update: (id: string) => `/users/${id}`,
    delete: (id: string) => `/users/${id}`,
  },
  
  // Clients
  clients: {
    list: '/clients',
    get: (id: string) => `/clients/${id}`,
    create: '/clients',
    update: (id: string) => `/clients/${id}`,
    delete: (id: string) => `/clients/${id}`,
  },
  
  // Appointments
  appointments: {
    list: '/appointments',
    get: (id: string) => `/appointments/${id}`,
    create: '/appointments',
    update: (id: string) => `/appointments/${id}`,
    delete: (id: string) => `/appointments/${id}`,
    byClient: (clientId: string) => `/appointments/client/${clientId}`,
  },
  
  // Sessions
  sessions: {
    list: '/sessions',
    get: (id: string) => `/sessions/${id}`,
    create: '/sessions',
    update: (id: string) => `/sessions/${id}`,
    delete: (id: string) => `/sessions/${id}`,
    byClient: (clientId: string) => `/sessions/client/${clientId}`,
  },
  
  // Academic Performance
  academicPerformance: {
    list: '/academic-performance',
    get: (id: string) => `/academic-performance/${id}`,
    create: '/academic-performance',
    update: (id: string) => `/academic-performance/${id}`,
    delete: (id: string) => `/academic-performance/${id}`,
    byStudent: (studentId: string) => `/academic-performance/student/${studentId}`,
    analytics: '/academic-performance/analytics',
    // SIS Results
    syncSis: '/academic-performance/sync/sis',
    syncClientSis: (clientId: string) => `/academic-performance/client/${clientId}/sync/sis`,
    cachedSis: (clientId: string) => `/academic-performance/client/${clientId}/cached/sis`,
    gpaTrend: (clientId: string) => `/academic-performance/client/${clientId}/gpa-trend`,
    atRisk: '/academic-performance/at-risk',
  },
  
  // Risk Assessments
  riskAssessments: {
    list: '/risk-assessments',
    get: (id: string) => `/risk-assessments/${id}`,
    create: '/risk-assessments',
    update: (id: string) => `/risk-assessments/${id}`,
    delete: (id: string) => `/risk-assessments/${id}`,
    byClient: (clientId: string) => `/risk-assessments/client/${clientId}`,
  },
  
  // Dashboard
  dashboard: {
    stats: '/dashboard/stats',
    recentAppointments: '/dashboard/recent-appointments',
    upcomingAppointments: '/dashboard/upcoming-appointments',
    atRiskStudents: '/dashboard/at-risk-students',
  },

  // Notifications
  notifications: {
    list: '/notifications',
    markAsRead: (id: string) => `/notifications/${id}/read`,
  },

  // Messages
  messages: {
    list: '/messages',
    get: (partnerId: string) => `/messages/${partnerId}`,
    create: '/messages',
    send: '/messages/send',
    unreadCount: '/messages/unread-count',
    markAsRead: (messageId: string) => `/messages/${messageId}/read`,
    markAllAsRead: '/messages/read-all',
    getConversations: '/conversations',
    markConversationAsRead: (partnerId: string) => `/conversations/${partnerId}/read`,
  },
};