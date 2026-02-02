import api from './api';
import { PaginatedResponse } from '../types/api';

/**
 * Session Type Definitions
 */
export interface Session {
  id: string;
  client: {
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
  appointment?: {
    id: string;
    title: string;
    appointmentDate: string;
  };
  sessionDate: string;
  duration: number;
  sessionType: string;
  notes?: string;
  followUpRequired: boolean;
  followUpDate?: string;
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  createdAt: string;
  updatedAt: string;
}

export interface CreateSessionRequest {
  appointmentId?: number;
  clientId: number;
  counselorId: number;
  sessionDate: string;
  duration: number;
  sessionType: string;
  notes?: string;
  followUpRequired?: boolean;
  followUpDate?: string;
}

export interface UpdateSessionRequest {
  sessionDate?: string;
  duration?: number;
  sessionType?: string;
  notes?: string;
  followUpRequired?: boolean;
  followUpDate?: string;
  status?: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
}

/**
 * Session Service - Handles all session/counseling session API operations
 * Required Roles: ADMIN, COUNSELOR
 */
export const sessionService = {
  /**
   * Get all sessions (paginated)
   * GET /api/v1/sessions?page=0&size=10
   */
  getAllSessions: async (page = 0, size = 10): Promise<PaginatedResponse<Session>> => {
    const response = await api.get(`/sessions?page=${page}&size=${size}`);
    return response.data.data;
  },

  /**
   * Get session by ID
   * GET /api/v1/sessions/{id}
   */
  getSessionById: async (id: string): Promise<Session> => {
    const response = await api.get(`/sessions/${id}`);
    return response.data.data;
  },

  /**
   * Create new session
   * POST /api/v1/sessions
   */
  createSession: async (sessionData: CreateSessionRequest): Promise<Session> => {
    const response = await api.post('/sessions', sessionData);
    return response.data.data;
  },

  /**
   * Update session
   * PUT /api/v1/sessions/{id}
   */
  updateSession: async (id: string, sessionData: UpdateSessionRequest): Promise<Session> => {
    const response = await api.put(`/sessions/${id}`, sessionData);
    return response.data.data;
  },

  /**
   * Delete session (Admin only)
   * DELETE /api/v1/sessions/{id}
   */
  deleteSession: async (id: string): Promise<void> => {
    await api.delete(`/sessions/${id}`);
  },

  /**
   * Get sessions by client
   * GET /api/v1/sessions/client/{clientId}
   */
  getSessionsByClient: async (clientId: string, page = 0, size = 10): Promise<PaginatedResponse<Session>> => {
    const response = await api.get(`/sessions/client/${clientId}?page=${page}&size=${size}`);
    return response.data.data;
  }
};
