import api from './api';
import { PaginatedResponse } from '../types/api';

/**
 * Risk Assessment Type Definitions
 */
export interface RiskAssessment {
  id: string;
  client: {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
  };
  assessmentType: string;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  score: number;
  factors: string[];
  recommendations: string[];
  assessedBy: {
    id: string;
    firstName: string;
    lastName: string;
  };
  assessmentDate: string;
  notes?: string;
  escalated: boolean;
  followUpRequired: boolean;
  followUpDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateRiskAssessmentRequest {
  clientId: number;
  assessmentType: string;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  score: number;
  factors: string[];
  recommendations: string[];
  notes?: string;
}

export interface UpdateRiskAssessmentRequest {
  assessmentType?: string;
  riskLevel?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  score?: number;
  factors?: string[];
  recommendations?: string[];
  notes?: string;
  escalated?: boolean;
  followUpRequired?: boolean;
  followUpDate?: string;
}

/**
 * Risk Assessment Service - Handles all risk assessment API operations
 * Required Roles: ADMIN, COUNSELOR
 */
export const riskAssessmentService = {
  /**
   * Get all risk assessments (paginated)
   * GET /api/v1/risk-assessments?page=0&size=10
   */
  getAllRiskAssessments: async (page = 0, size = 10): Promise<PaginatedResponse<RiskAssessment>> => {
    const response = await api.get(`/risk-assessments?page=${page}&size=${size}`);
    return response.data.data;
  },

  /**
   * Get risk assessment by ID
   * GET /api/v1/risk-assessments/{id}
   */
  getRiskAssessmentById: async (id: string): Promise<RiskAssessment> => {
    const response = await api.get(`/risk-assessments/${id}`);
    return response.data.data;
  },

  /**
   * Create risk assessment
   * POST /api/v1/risk-assessments
   */
  createRiskAssessment: async (data: CreateRiskAssessmentRequest): Promise<RiskAssessment> => {
    const response = await api.post('/risk-assessments', data);
    return response.data.data;
  },

  /**
   * Update risk assessment
   * PUT /api/v1/risk-assessments/{id}
   */
  updateRiskAssessment: async (id: string, data: UpdateRiskAssessmentRequest): Promise<RiskAssessment> => {
    const response = await api.put(`/risk-assessments/${id}`, data);
    return response.data.data;
  },

  /**
   * Delete risk assessment (Admin only)
   * DELETE /api/v1/risk-assessments/{id}
   */
  deleteRiskAssessment: async (id: string): Promise<void> => {
    await api.delete(`/risk-assessments/${id}`);
  },

  /**
   * Get risk assessments by client
   * GET /api/v1/risk-assessments/client/{clientId}
   */
  getRiskAssessmentsByClient: async (clientId: string): Promise<RiskAssessment[]> => {
    const response = await api.get(`/risk-assessments/client/${clientId}`);
    return response.data.data;
  },

  /**
   * Get high risk assessments
   * GET /api/v1/risk-assessments/high-risk
   */
  getHighRiskAssessments: async (): Promise<RiskAssessment[]> => {
    const response = await api.get('/risk-assessments/high-risk');
    return response.data.data;
  },

  /**
   * Get risk assessment statistics
   * GET /api/v1/risk-assessments/stats
   */
  getStats: async () => {
    const response = await api.get('/risk-assessments/stats');
    return response.data.data;
  },

  /**
   * Escalate risk assessment
   * POST /api/v1/risk-assessments/{id}/escalate
   */
  escalateRiskAssessment: async (id: string): Promise<RiskAssessment> => {
    const response = await api.post(`/risk-assessments/${id}/escalate`);
    return response.data.data;
  },

  /**
   * Export risk assessments (Admin only)
   * GET /api/v1/risk-assessments/export?format=csv
   */
  exportRiskAssessments: async (format: 'csv' | 'pdf' = 'csv'): Promise<Blob> => {
    const response = await api.get(`/risk-assessments/export?format=${format}`, {
      responseType: 'blob'
    });
    return response.data;
  },

  /**
   * Get latest risk assessment for client
   * GET /api/v1/risk-assessments/client/{clientId}/latest
   */
  getLatestForClient: async (clientId: string): Promise<RiskAssessment> => {
    const response = await api.get(`/risk-assessments/client/${clientId}/latest`);
    return response.data.data;
  },

  /**
   * Get risk assessment trend for client
   * GET /api/v1/risk-assessments/client/{clientId}/trend
   */
  getTrend: async (clientId: string) => {
    const response = await api.get(`/risk-assessments/client/${clientId}/trend`);
    return response.data.data;
  },

  /**
   * Get risk assessment summary
   * GET /api/v1/risk-assessments/summary
   */
  getSummary: async () => {
    const response = await api.get('/risk-assessments/summary');
    return response.data.data;
  },

  /**
   * Get assessments requiring follow-up
   * GET /api/v1/risk-assessments/follow-up-required
   */
  getFollowUpRequired: async (): Promise<RiskAssessment[]> => {
    const response = await api.get('/risk-assessments/follow-up-required');
    return response.data.data;
  },

  /**
   * Get assessments by assessor
   * GET /api/v1/risk-assessments/assessor?assessorId={id}
   */
  getByAssessor: async (assessorId: string) => {
    const response = await api.get(`/risk-assessments/assessor?assessorId=${assessorId}`);
    return response.data.data;
  },

  /**
   * Get risk assessment analytics
   * GET /api/v1/risk-assessments/analytics
   */
  getAnalytics: async () => {
    const response = await api.get('/risk-assessments/analytics');
    return response.data.data;
  }
};
