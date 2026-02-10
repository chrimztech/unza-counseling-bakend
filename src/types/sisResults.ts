// SIS Results Types

export interface StudentCourseHistory {
  courseCode: string;
  courseTitle: string;
  creditHours?: number;
  semester?: string;
  academicYear?: string;
  grade?: string;
  gradePoint?: number;
  marks?: number;
  status?: string;
  courseType?: string;
  assessmentType?: string;
}

export interface StudentInfo {
  studentId?: string;
  firstName?: string;
  lastName?: string;
  programme?: string;
  faculty?: string;
  department?: string;
  yearOfStudy?: number;
  currentGpa?: number;
  cumulativeGpa?: number;
  totalCreditsEarned?: number;
  totalCreditsAttempted?: number;
  classification?: string;
}

export interface ResultsSummary {
  totalCourses?: number;
  passedCourses?: number;
  failedCourses?: number;
  withdrawnCourses?: number;
  incompleteCourses?: number;
  averageGrade?: number;
  averageGpa?: number;
  currentGpa?: number;
  cumulativeGpa?: number;
  totalCreditsEarned?: number;
  totalCreditsAttempted?: number;
  academicStanding?: string;
  performanceTrend?: string;
}

export interface SyncResultsResponse {
  success: boolean;
  message: string;
  errorType?: string;
  summary?: ResultsSummary;
  courses?: StudentCourseHistory[];
  studentInfo?: StudentInfo;
}

// Mental Health Analysis Types
export interface AcademicRiskAssessment {
  riskLevel: 'LOW' | 'MODERATE' | 'HIGH';
  riskFactors: string[];
  recommendations: string[];
  academicSummary: {
    gpa: number;
    passRate: number;
    failedCourses: number;
  };
}

export interface PerformanceTrend {
  trend: 'IMPROVING' | 'DECLINING' | 'STABLE';
  semesterData: { [key: string]: number };
  analysis: string;
}
