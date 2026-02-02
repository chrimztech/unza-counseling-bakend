import api from './api';
import { PaginatedResponse } from '../types/api';

/**
 * Appointment Type Definitions
 */
export interface Appointment {
  id: string;
  title: string;
  student: {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
  };
  counselor: {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
  };
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

export interface CreateAppointmentRequest {
  studentId: number;
  counselorId: number;
  title: string;
  description?: string;
  appointmentDate: string; // ISO format: "2026-02-05T10:00:00"
  type: 'INITIAL_CONSULTATION' | 'FOLLOW_UP' | 'GROUP_SESSION' | 'ASSESSMENT' | 'CRISIS_INTERVENTION';
}

export interface UpdateAppointmentRequest {
  title?: string;
  description?: string;
  appointmentDate?: string;
  type?: 'INITIAL_CONSULTATION' | 'FOLLOW_UP' | 'GROUP_SESSION' | 'ASSESSMENT' | 'CRISIS_INTERVENTION';
  status?: 'SCHEDULED' | 'CONFIRMED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW' | 'RESCHEDULED';
}

/**
 * Appointment Service - Handles all appointment-related API operations
 * Required Roles: All authenticated users (varies by endpoint)
 */
export const appointmentService = {
  /**
   * Get all appointments (paginated)
   * GET /api/v1/appointments?page=0&size=10
   */
  getAllAppointments: async (page = 0, size = 10): Promise<PaginatedResponse<Appointment>> => {
    const response = await api.get(`/appointments?page=${page}&size=${size}`);
    return response.data.data;
  },

  /**
   * Get appointment by ID
   * GET /api/v1/appointments/{id}
   */
  getAppointmentById: async (id: string): Promise<Appointment> => {
    const response = await api.get(`/appointments/${id}`);
    return response.data.data;
  },

  /**
   * Create new appointment
   * POST /api/v1/appointments
   */
  createAppointment: async (appointmentData: CreateAppointmentRequest): Promise<Appointment> => {
    const response = await api.post('/appointments', appointmentData);
    return response.data.data;
  },

  /**
   * Update appointment
   * PUT /api/v1/appointments/{id}
   */
  updateAppointment: async (id: string, appointmentData: UpdateAppointmentRequest): Promise<Appointment> => {
    const response = await api.put(`/appointments/${id}`, appointmentData);
    return response.data.data;
  },

  /**
   * Get appointments by counselor
   * GET /api/v1/appointments/counselor/{counselorId}
   */
  getAppointmentsByCounselor: async (counselorId: string, page = 0, size = 10): Promise<PaginatedResponse<Appointment>> => {
    const response = await api.get(`/appointments/counselor/${counselorId}?page=${page}&size=${size}`);
    return response.data.data;
  },

  /**
   * Get appointments by student
   * GET /api/v1/appointments/student/{studentId}
   */
  getAppointmentsByStudent: async (studentId: string, page = 0, size = 10): Promise<PaginatedResponse<Appointment>> => {
    const response = await api.get(`/appointments/student/${studentId}?page=${page}&size=${size}`);
    return response.data.data;
  },

  /**
   * Get appointments by client
   * GET /api/v1/appointments/client/{clientId}
   */
  getAppointmentsByClient: async (clientId: string, page = 0, size = 10): Promise<PaginatedResponse<Appointment>> => {
    const response = await api.get(`/appointments/client/${clientId}?page=${page}&size=${size}`);
    return response.data.data;
  },

  /**
   * Get upcoming appointments
   * GET /api/v1/appointments/upcoming
   */
  getUpcomingAppointments: async (page = 0, size = 10): Promise<PaginatedResponse<Appointment>> => {
    const response = await api.get(`/appointments/upcoming?page=${page}&size=${size}`);
    return response.data.data;
  },

  /**
   * Get past appointments
   * GET /api/v1/appointments/past
   */
  getPastAppointments: async (page = 0, size = 10): Promise<PaginatedResponse<Appointment>> => {
    const response = await api.get(`/appointments/past?page=${page}&size=${size}`);
    return response.data.data;
  },

  /**
   * Get cancelled appointments
   * GET /api/v1/appointments/cancelled
   */
  getCancelledAppointments: async (page = 0, size = 10): Promise<PaginatedResponse<Appointment>> => {
    const response = await api.get(`/appointments/cancelled?page=${page}&size=${size}`);
    return response.data.data;
  },

  /**
   * Get confirmed appointments
   * GET /api/v1/appointments/confirmed
   */
  getConfirmedAppointments: async (page = 0, size = 10): Promise<PaginatedResponse<Appointment>> => {
    const response = await api.get(`/appointments/confirmed?page=${page}&size=${size}`);
    return response.data.data;
  },

  /**
   * Get pending appointments
   * GET /api/v1/appointments/pending
   */
  getPendingAppointments: async (page = 0, size = 10): Promise<PaginatedResponse<Appointment>> => {
    const response = await api.get(`/appointments/pending?page=${page}&size=${size}`);
    return response.data.data;
  },

  /**
   * Get today's appointments
   * GET /api/v1/appointments/today
   */
  getTodaysAppointments: async (page = 0, size = 10): Promise<PaginatedResponse<Appointment>> => {
    const response = await api.get(`/appointments/today?page=${page}&size=${size}`);
    return response.data.data;
  },

  /**
   * Cancel appointment
   * PUT /api/v1/appointments/{id}/cancel
   */
  cancelAppointment: async (id: string, reason?: string): Promise<Appointment> => {
    const response = await api.put(`/appointments/${id}/cancel`, { cancellationReason: reason });
    return response.data.data;
  },

  /**
   * Confirm appointment (Admin/Counselor only)
   * PUT /api/v1/appointments/{id}/confirm
   */
  confirmAppointment: async (id: string): Promise<Appointment> => {
    const response = await api.put(`/appointments/${id}/confirm`);
    return response.data.data;
  },

  /**
   * Reschedule appointment
   * PUT /api/v1/appointments/{id}/reschedule
   */
  rescheduleAppointment: async (id: string, newData: {
    appointmentDate: string;
    title?: string;
    description?: string;
  }): Promise<Appointment> => {
    const response = await api.put(`/appointments/${id}/reschedule`, newData);
    return response.data.data;
  },

  /**
   * Check counselor availability
   * GET /api/v1/appointments/availability?counselorId={id}&date={date}
   */
  checkAvailability: async (counselorId: string, date: string): Promise<string[]> => {
    const response = await api.get(`/appointments/availability?counselorId=${counselorId}&date=${date}`);
    return response.data.data;
  },

  /**
   * Get appointment statistics
   * GET /api/v1/appointments/stats
   */
  getAppointmentStatistics: async () => {
    const response = await api.get('/appointments/stats');
    return response.data.data;
  },

  /**
   * Export appointments to CSV/PDF (Admin only)
   * GET /api/v1/appointments/export?format=csv&startDate=&endDate=
   */
  exportAppointments: async (
    format: 'csv' | 'pdf' = 'csv',
    startDate?: string,
    endDate?: string
  ): Promise<Blob> => {
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
