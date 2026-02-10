/**
 * Consent Service - Handles all consent-related API calls
 * 
 * Usage: Copy this file to your React project's src/services/ directory
 * Make sure you have axios installed: npm install axios
 */

import axios, { AxiosInstance } from 'axios';

// API Base URL - Change this to match your backend URL
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Create axios instance
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

// Add auth token to requests
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Consent Form Response DTO
export interface ConsentFormResponse {
  id: number;
  title: string;
  content: string;
  version: string;
  active: boolean;
  effectiveDate: string;
  createdAt: string;
  updatedAt: string;
}

// User Consent Response DTO
export interface UserConsentResponse {
  id: number;
  userId: number;
  consentFormId: number;
  consentFormTitle: string;
  consentFormVersion: string;
  consentDate: string;
  ipAddress: string;
  userAgent: string;
  createdAt: string;
}

// Sign Consent Request DTO
export interface SignConsentRequest {
  consentFormId: number;
  ipAddress?: string;
  userAgent?: string;
}

const consentService = {
  /**
   * Check if the current user has signed the latest active consent form
   */
  checkSignedConsent: async (): Promise<boolean> => {
    try {
      const response = await apiClient.get<boolean>('/consent/check-signed');
      return response.data;
    } catch (error) {
      console.error('Failed to check consent status:', error);
      // If error or no consent form exists, treat as no consent required
      return true;
    }
  },

  /**
   * Get the latest active consent form
   */
  getLatestActiveConsentForm: async (): Promise<ConsentFormResponse | null> => {
    try {
      const response = await apiClient.get<ConsentFormResponse>('/consent/forms/latest');
      return response.data;
    } catch (error) {
      console.error('Failed to fetch latest consent form:', error);
      return null;
    }
  },

  /**
   * Get all active consent forms
   */
  getActiveConsentForms: async (): Promise<ConsentFormResponse[]> => {
    try {
      const response = await apiClient.get<ConsentFormResponse[]>('/consent/forms/active');
      return response.data;
    } catch (error) {
      console.error('Failed to fetch active consent forms:', error);
      return [];
    }
  },

  /**
   * Sign a consent form
   */
  signConsent: async (request: SignConsentRequest): Promise<UserConsentResponse> => {
    const enrichedRequest = {
      ...request,
      ipAddress: request.ipAddress || 'unknown',
      userAgent: request.userAgent || navigator.userAgent,
    };

    const response = await apiClient.post<UserConsentResponse>('/consent/sign', enrichedRequest);
    return response.data;
  },

  /**
   * Get user's consent history
   */
  getUserConsentHistory: async (): Promise<UserConsentResponse[]> => {
    try {
      const response = await apiClient.get<UserConsentResponse[]>('/consent/history');
      return response.data;
    } catch (error) {
      console.error('Failed to fetch consent history:', error);
      return [];
    }
  },
};

export default consentService;
