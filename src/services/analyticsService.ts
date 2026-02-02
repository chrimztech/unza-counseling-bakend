import api from './api';
import { DashboardStats } from '../types/api';

/**
 * Analytics Service - Handles all analytics and reporting API operations
 * Required Roles: ADMIN, COUNSELOR
 */
export const analyticsService = {
  /**
   * Get analytics overview (same as dashboard stats)
   * GET /api/v1/analytics/overview
   */
  getOverview: async (): Promise<DashboardStats> => {
    const response = await api.get('/analytics/overview');
    return response.data.data;
  },

  /**
   * Get intervention report
   * GET /api/v1/analytics/intervention-report
   */
  getInterventionReport: async () => {
    const response = await api.get('/analytics/intervention-report');
    return response.data.data;
  },

  /**
   * Get counselor performance analytics
   * GET /api/v1/analytics/counselor-performance
   */
  getCounselorPerformance: async () => {
    const response = await api.get('/analytics/counselor-performance');
    return response.data.data;
  },

  /**
   * Get client demographics
   * GET /api/v1/analytics/client-demographics
   */
  getClientDemographics: async () => {
    const response = await api.get('/analytics/client-demographics');
    return response.data.data;
  },

  /**
   * Get session analytics
   * GET /api/v1/analytics/session-analytics
   */
  getSessionAnalytics: async () => {
    const response = await api.get('/analytics/session-analytics');
    return response.data.data;
  },

  /**
   * Get risk assessment analytics
   * GET /api/v1/analytics/risk-assessment
   */
  getRiskAssessmentAnalytics: async () => {
    const response = await api.get('/analytics/risk-assessment');
    return response.data.data;
  },

  /**
   * Get time analysis
   * GET /api/v1/analytics/time-analysis
   */
  getTimeAnalysis: async () => {
    const response = await api.get('/analytics/time-analysis');
    return response.data.data;
  },

  /**
   * Get outcomes analytics
   * GET /api/v1/analytics/outcomes
   */
  getOutcomesAnalytics: async () => {
    const response = await api.get('/analytics/outcomes');
    return response.data.data;
  },

  /**
   * Export analytics data (Admin only)
   * GET /api/v1/analytics/export?format=csv
   */
  exportAnalytics: async (format: 'csv' | 'pdf' | 'excel' = 'csv'): Promise<Blob> => {
    const response = await api.get(`/analytics/export?format=${format}`, {
      responseType: 'blob'
    });
    return response.data;
  }
};
