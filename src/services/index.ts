// Export all services from a single entry point

export { default as api, endpoints } from './api';
export { authService } from './authService';
export { userService } from './userService';
export { clientService } from './clientService';
export { appointmentService } from './appointmentService';
export { sessionService } from './sessionService';
export { counselorService } from './counselorService';
export { notificationService } from './notificationService';
export { riskAssessmentService } from './riskAssessmentService';
export { analyticsService } from './analyticsService';
export { dashboardService } from './dashboardService';

// Re-export types
export type { Appointment, CreateAppointmentRequest, UpdateAppointmentRequest } from './appointmentService';
export type { Session, CreateSessionRequest, UpdateSessionRequest } from './sessionService';
export type { Counselor } from './counselorService';
export type { Notification } from './notificationService';
export type { RiskAssessment, CreateRiskAssessmentRequest, UpdateRiskAssessmentRequest } from './riskAssessmentService';
