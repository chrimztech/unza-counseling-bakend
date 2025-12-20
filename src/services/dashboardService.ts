import api, { endpoints } from './api';
import { DashboardStats } from '../types/api';

// Dashboard Service
export const dashboardService = {
  // Get dashboard statistics
  getStats: async (): Promise<DashboardStats> => {
    const response = await api.get(endpoints.dashboard.stats);
    return response.data;
  },

  // Get recent appointments
  getRecentAppointments: async () => {
    const response = await api.get(endpoints.dashboard.recentAppointments);
    return response.data;
  },

  // Get upcoming appointments
  getUpcomingAppointments: async () => {
    const response = await api.get(endpoints.dashboard.upcomingAppointments);
    return response.data;
  },

  // Get at-risk students
  getAtRiskStudents: async () => {
    const response = await api.get(endpoints.dashboard.atRiskStudents);
    return response.data;
  },
};