# UNZA Counseling System - Frontend Integration Guide (Part 2)

Continuation of the integration guide covering remaining endpoints.

---

## 12. Report Endpoints

**Required Roles:** ADMIN, COUNSELOR

```typescript
// Create src/services/reportService.ts
import api from './api';

export interface Report {
  id: string;
  reportType: string;
  title: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  status: 'PENDING' | 'GENERATING' | 'COMPLETED' | 'FAILED';
  fileUrl?: string;
  createdBy: string;
  createdAt: string;
  completedAt?: string;
}

export const reportService = {
  // Get all reports (paginated)
  getAllReports: async (page = 0, size{