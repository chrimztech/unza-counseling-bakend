import api from './api';
import {
  SyncResultsResponse,
  StudentCourseHistory,
  ResultsSummary,
  StudentInfo,
  AcademicRiskAssessment,
  PerformanceTrend,
} from '../types/sisResults';

/**
 * SIS Results Service - Handles academic results from external SIS API
 */
export const sisResultsService = {
  /**
   * Sync results from SIS for a specific student ID
   * POST /api/v1/academic-performance/sync/sis
   */
  syncStudentResults: async (
    studentId: string,
    token?: string,
    forceRefresh: boolean = false
  ): Promise<SyncResultsResponse> => {
    const params = new URLSearchParams();
    params.append('student_id', studentId);
    if (token) params.append('token', token);
    params.append('forceRefresh', forceRefresh.toString());

    const response = await api.post(
      `/academic-performance/sync/sis?${params.toString()}`
    );
    return response.data.data;
  },

  /**
   * Sync results for a client by their internal ID
   * POST /api/v1/academic-performance/client/{clientId}/sync/sis
   */
  syncClientResults: async (
    clientId: string,
    token?: string,
    forceRefresh: boolean = false
  ): Promise<SyncResultsResponse> => {
    const params = new URLSearchParams();
    if (token) params.append('token', token);
    params.append('forceRefresh', forceRefresh.toString());

    const response = await api.post(
      `/academic-performance/client/${clientId}/sync/sis?${params.toString()}`
    );
    return response.data.data;
  },

  /**
   * Get cached results for a client (offline support)
   * GET /api/v1/academic-performance/client/{clientId}/cached/sis
   */
  getCachedResults: async (clientId: string): Promise<SyncResultsResponse> => {
    const response = await api.get(
      `/academic-performance/client/${clientId}/cached/sis`
    );
    return response.data.data;
  },

  /**
   * Get all academic performance records for a client
   * GET /api/v1/academic-performance/client/{clientId}
   */
  getClientAcademicRecords: async (clientId: string) => {
    const response = await api.get(
      `/academic-performance/client/${clientId}`
    );
    return response.data.data;
  },

  /**
   * Get academic performance summary for a client
   * GET /api/v1/academic-performance/client/{clientId}/summary
   */
  getClientAcademicSummary: async (clientId: string) => {
    const response = await api.get(
      `/academic-performance/client/${clientId}/summary`
    );
    return response.data.data;
  },

  /**
   * Get GPA trend data for a client
   * GET /api/v1/academic-performance/client/{clientId}/gpa-trend
   */
  getGpaTrend: async (clientId: string) => {
    const response = await api.get(
      `/academic-performance/client/${clientId}/gpa-trend`
    );
    return response.data.data;
  },

  /**
   * Get students at risk based on academic performance
   * GET /api/v1/academic-performance/at-risk
   */
  getStudentsAtRisk: async () => {
    const response = await api.get('/academic-performance/at-risk');
    return response.data.data;
  },
};

/**
 * Mental Health Analysis Service
 * Analyzes academic performance impact on mental health
 */
export const mentalHealthAnalysisService = {
  /**
   * Analyze academic performance impact on mental health
   */
  analyzeAcademicImpact: (summary: ResultsSummary): AcademicRiskAssessment => {
    const riskFactors: string[] = [];
    const recommendations: string[] = [];

    // Check for academic stress indicators
    if (summary.averageGpa && summary.averageGpa < 2.0) {
      riskFactors.push('Low GPA below 2.0 - Academic probation risk');
      recommendations.push('Consider academic counseling support');
    }

    const passRate =
      summary.totalCourses && summary.totalCourses > 0
        ? (summary.passedCourses || 0) / summary.totalCourses
        : 0;

    if (passRate < 0.7) {
      riskFactors.push('Pass rate below 70%');
      recommendations.push('Review study habits and time management');
    }

    if (summary.failedCourses && summary.failedCourses > 2) {
      riskFactors.push('Multiple failed courses');
      recommendations.push('Discuss course difficulty with advisors');
    }

    // Calculate overall risk level
    let riskLevel: 'LOW' | 'MODERATE' | 'HIGH' = 'LOW';
    if (riskFactors.length >= 3) {
      riskLevel = 'HIGH';
    } else if (riskFactors.length > 0) {
      riskLevel = 'MODERATE';
    }

    return {
      riskLevel,
      riskFactors,
      recommendations,
      academicSummary: {
        gpa: summary.averageGpa || 0,
        passRate: passRate * 100,
        failedCourses: summary.failedCourses || 0,
      },
    };
  },

  /**
   * Analyze performance trends
   */
  analyzeTrends: (courses: StudentCourseHistory[]): PerformanceTrend => {
    // Group by semester
    const bySemester: { [key: string]: StudentCourseHistory[] } = {};
    for (const course of courses) {
      const key = `${course.academicYear}_${course.semester}`;
      if (!bySemester[key]) {
        bySemester[key] = [];
      }
      bySemester[key].push(course);
    }

    // Calculate GPA per semester
    const semesterData: { [key: string]: number } = {};
    const sortedKeys = Object.keys(bySemester).sort();

    for (const key of sortedKeys) {
      const semesterCourses = bySemester[key];
      const validGpas = semesterCourses
        .filter((c) => c.gradePoint != null)
        .map((c) => c.gradePoint!);
      if (validGpas.length > 0) {
        semesterData[key] =
          validGpas.reduce((a, b) => a + b, 0) / validGpas.length;
      }
    }

    // Determine trend
    let trend: 'IMPROVING' | 'DECLINING' | 'STABLE' = 'STABLE';
    const sortedSemesters = Object.keys(semesterData).sort();

    if (sortedSemesters.length >= 2) {
      const recent = semesterData[sortedSemesters[sortedSemesters.length - 1]];
      const previous =
        semesterData[sortedSemesters[sortedSemesters.length - 2]];

      if (recent > previous + 0.2) {
        trend = 'IMPROVING';
      } else if (recent < previous - 0.2) {
        trend = 'DECLINING';
      }
    }

    let analysis = 'Academic performance is stable. Maintain consistent effort.';
    if (trend === 'IMPROVING') {
      analysis =
        'Academic performance is improving. Continue with current study strategies.';
    } else if (trend === 'DECLINING') {
      analysis =
        'Academic performance is declining. Consider seeking academic support early.';
    }

    return {
      trend,
      semesterData,
      analysis,
    };
  },

  /**
   * Generate comprehensive academic mental health report
   */
  generateComprehensiveReport: (
    courses: StudentCourseHistory[],
    summary: ResultsSummary
  ) => {
    const riskAssessment = mentalHealthAnalysisService.analyzeAcademicImpact(summary);
    const trendAnalysis = mentalHealthAnalysisService.analyzeTrends(courses);

    return {
      riskAssessment,
      trendAnalysis,
      generatedAt: new Date().toISOString(),
      recommendations: [
        ...riskAssessment.recommendations,
        trendAnalysis.trend === 'DECLINING'
          ? 'Schedule a counseling session to discuss academic challenges'
          : null,
      ].filter(Boolean),
    };
  },
};

export default {
  sisResultsService,
  mentalHealthAnalysisService,
};
