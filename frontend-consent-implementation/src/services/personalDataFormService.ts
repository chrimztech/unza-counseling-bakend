/**
 * Personal Data Form Service - Handles all personal data form-related API calls
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

// Interfaces for Personal Data Form
export interface PersonalDataFormResponse {
  id: number;
  clientFileNo: string;
  clientId: number;
  clientName: string;
  dateOfInterview: string;
  gender: 'MALE' | 'FEMALE' | 'OTHER' | 'PREFER_NOT_TO_SAY';
  yearOfBirth: number;
  school: string;
  computerNo: string;
  yearOfStudy: number;
  occupation: string;
  contactAddress: string;
  phoneNumber: string;
  maritalStatus: 'SINGLE' | 'MARRIED' | 'DIVORCED' | 'SEPARATED' | 'WIDOWED' | 'LIVING_TOGETHER';
  previousCounseling: PreviousCounselingType[];
  otherPreviousCounseling: string;
  referralSources: ReferralSource[];
  otherReferralSource: string;
  counselingReasons: CounselingReason[];
  otherCounselingReason: string;
  familyMembers: string[];
  goodHealth: boolean;
  healthCondition: string;
  takingMedication: boolean;
  medicationDetails: string;
  additionalInformation: string;
  createdAt: string;
  updatedAt: string;
}

export enum PreviousCounselingType {
  UNIVERSITY_COUNSELING = 'UNIVERSITY_COUNSELING',
  SUBJECT_COUNSELOR = 'SUBJECT_COUNSELOR',
  OTHER = 'OTHER',
  NONE = 'NONE'
}

export enum ReferralSource {
  SELF = 'SELF',
  SUBJECT_COUNSELOR = 'SUBJECT_COUNSELOR',
  FRIEND = 'FRIEND',
  PARTNER = 'PARTNER',
  FAMILY_MEMBER = 'FAMILY_MEMBER',
  HEALTH_WORKER = 'HEALTH_WORKER',
  OTHER = 'OTHER'
}

export enum CounselingReason {
  // Personal Concerns
  PERSONAL_TRANSITIONS = 'PERSONAL_TRANSITIONS',
  PERSONAL_RELATIONSHIPS = 'PERSONAL_RELATIONSHIPS',
  PERSONAL_WORRY = 'PERSONAL_WORRY',
  PERSONAL_FAMILY = 'PERSONAL_FAMILY',
  PERSONAL_ALCOHOL_DRUGS_SEXUAL_ABUSE = 'PERSONAL_ALCOHOL_DRUGS_SEXUAL_ABUSE',
  PERSONAL_GUILTY_FEELINGS = 'PERSONAL_GUILTY_FEELINGS',
  PERSONAL_BEREAVEMENT = 'PERSONAL_BEREAVEMENT',
  PERSONAL_COMMUNICATION_CONFLICT = 'PERSONAL_COMMUNICATION_CONFLICT',

  // Health Concerns
  HEALTH_HIV_STI = 'HEALTH_HIV_STI',
  HEALTH_FAMILY_PLANNING = 'HEALTH_FAMILY_PLANNING',
  HEALTH_PREGNANT = 'HEALTH_PREGNANT',
  HEALTH_UNWELL = 'HEALTH_UNWELL',

  // Educational Concerns
  EDUCATIONAL_CHANGE_COURSE = 'EDUCATIONAL_CHANGE_COURSE',
  EDUCATIONAL_WITHDRAWAL = 'EDUCATIONAL_WITHDRAWAL',
  EDUCATIONAL_EXAM_ANXIETY = 'EDUCATIONAL_EXAM_ANXIETY',
  EDUCATIONAL_MISSED_ASSIGNMENTS = 'EDUCATIONAL_MISSED_ASSIGNMENTS',
  EDUCATIONAL_EXCLUDED = 'EDUCATIONAL_EXCLUDED',
  EDUCATIONAL_LEAVE = 'EDUCATIONAL_LEAVE',
  EDUCATIONAL_RESEARCH = 'EDUCATIONAL_RESEARCH',
  EDUCATIONAL_LACK_CONCENTRATION = 'EDUCATIONAL_LACK_CONCENTRATION',

  // Career/Vocational Guidance
  CAREER_CHOICE = 'CAREER_CHOICE',
  CAREER_EMPLOYMENT_SEARCH = 'CAREER_EMPLOYMENT_SEARCH',
  CAREER_INTERVIEW_TECHNIQUES = 'CAREER_INTERVIEW_TECHNIQUES',
  CAREER_CV_WRITING = 'CAREER_CV_WRITING',
  CAREER_NO_GOALS = 'CAREER_NO_GOALS',
  CAREER_INTERNSHIP = 'CAREER_INTERNSHIP',

  // Financial/Tuition Related
  FINANCIAL_BURSARIES = 'FINANCIAL_BURSARIES',
  FINANCIAL_BANKING = 'FINANCIAL_BANKING',
  FINANCIAL_CAMPUS_WORK = 'FINANCIAL_CAMPUS_WORK',
  FINANCIAL_HARDSHIP_LOAN = 'FINANCIAL_HARDSHIP_LOAN'
}

export interface PersonalDataFormRequest {
  clientFileNo?: string;
  dateOfInterview: string;
  gender: 'MALE' | 'FEMALE' | 'OTHER' | 'PREFER_NOT_TO_SAY';
  yearOfBirth: number;
  school?: string;
  computerNo?: string;
  yearOfStudy?: number;
  occupation?: string;
  contactAddress?: string;
  phoneNumber?: string;
  maritalStatus?: 'SINGLE' | 'MARRIED' | 'DIVORCED' | 'SEPARATED' | 'WIDOWED' | 'LIVING_TOGETHER';
  previousCounseling?: PreviousCounselingType[];
  otherPreviousCounseling?: string;
  referralSources?: ReferralSource[];
  otherReferralSource?: string;
  counselingReasons?: CounselingReason[];
  otherCounselingReason?: string;
  familyMembers?: string[];
  goodHealth?: boolean;
  healthCondition?: string;
  takingMedication?: boolean;
  medicationDetails?: string;
  additionalInformation?: string;
}

const personalDataFormService = {
  /**
   * Create a new personal data form for a client
   */
  createPersonalDataForm: async (clientId: number, request: PersonalDataFormRequest): Promise<PersonalDataFormResponse> => {
    const response = await apiClient.post<PersonalDataFormResponse>(`/v1/personal-data-forms/clients/${clientId}`, request);
    return response.data;
  },

  /**
   * Get personal data form by client ID
   */
  getPersonalDataFormByClientId: async (clientId: number): Promise<PersonalDataFormResponse> => {
    const response = await apiClient.get<PersonalDataFormResponse>(`/v1/personal-data-forms/clients/${clientId}`);
    return response.data;
  },

  /**
   * Get personal data form by client file number
   */
  getPersonalDataFormByClientFileNo: async (clientFileNo: string): Promise<PersonalDataFormResponse> => {
    const response = await apiClient.get<PersonalDataFormResponse>(`/v1/personal-data-forms/file/${clientFileNo}`);
    return response.data;
  },

  /**
   * Update a personal data form
   */
  updatePersonalDataForm: async (clientId: number, request: PersonalDataFormRequest): Promise<PersonalDataFormResponse> => {
    const response = await apiClient.put<PersonalDataFormResponse>(`/v1/personal-data-forms/clients/${clientId}`, request);
    return response.data;
  },

  /**
   * Delete a personal data form
   */
  deletePersonalDataForm: async (clientId: number): Promise<void> => {
    await apiClient.delete(`/v1/personal-data-forms/clients/${clientId}`);
  },
};

export default personalDataFormService;
