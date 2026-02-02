import api from './api';

/**
 * Counselor Type Definitions
 */
export interface Counselor {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  specialization?: string;
  qualifications?: string;
  yearsOfExperience?: number;
  licenseNumber?: string;
  availableForAppointments: boolean;
  department?: string;
  bio?: string;
  profilePicture?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Counselor Service - Handles all counselor API operations
 * Required Roles: Any authenticated user
 */
export const counselorService = {
  /**
   * Get all counselors
   * GET /api/v1/counselors
   */
  getAllCounselors: async (): Promise<Counselor[]> => {
    const response = await api.get('/counselors');
    return response.data;
  },

  /**
   * Get counselor by ID
   * GET /api/v1/counselors/{id}
   */
  getCounselorById: async (id: string): Promise<Counselor> => {
    const response = await api.get(`/counselors/${id}`);
    return response.data;
  },

  /**
   * Get available counselors (for appointment booking)
   * GET /api/v1/counselors/available
   */
  getAvailableCounselors: async (): Promise<Counselor[]> => {
    const response = await api.get('/counselors/available');
    return response.data;
  }
};
