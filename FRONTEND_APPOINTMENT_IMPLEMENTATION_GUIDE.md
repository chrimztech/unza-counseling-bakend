# Frontend Implementation Guide - Appointment & Booking System

This guide provides detailed instructions for implementing the appointment and booking system in your React frontend.

## API Endpoints

All endpoints are prefixed with `/api/appointments` and require JWT authentication.

### 1. Create Appointment

**Endpoint:** `POST /api/appointments`

**Request Body:**
```json
{
  "studentId": 1,
  "counselorId": 2,
  "title": "Initial Consultation",
  "appointmentDate": "2025-02-15T10:00:00",
  "type": "INDIVIDUAL",
  "description": "First session to discuss academic stress",
  "duration": 60
}
```

**Response:**
```json
{
  "success": true,
  "message": "Appointment created successfully",
  "data": {
    "id": 1,
    "title": "Initial Consultation",
    "studentId": 1,
    "counselorId": 2,
    "appointmentDate": "2025-02-15T10:00:00",
    "type": "INDIVIDUAL",
    "status": "SCHEDULED",
    "duration": 60,
    "description": "First session to discuss academic stress"
  }
}
```

### 2. Get All Appointments (Paginated)

**Endpoint:** `GET /api/appointments?page=0&size=10`

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Initial Consultation",
      "studentId": 1,
      "studentName": "John Doe",
      "counselorId": 2,
      "counselorName": "Dr. Smith",
      "appointmentDate": "2025-02-15T10:00:00",
      "type": "INDIVIDUAL",
      "status": "SCHEDULED",
      "duration": 60
    }
  ],
  "totalElements": 50,
  "totalPages": 5,
  "page": 0,
  "size": 10
}
```

### 3. Get Appointment by ID

**Endpoint:** `GET /api/appointments/{id}`

### 4. Get Appointments by Client

**Endpoint:** `GET /api/appointments/client/{clientId}?page=0&size=10`

### 5. Get Appointments by Counselor

**Endpoint:** `GET /api/appointments/counselor/{counselorId}?page=0&size=10`

### 6. Get Upcoming Appointments

**Endpoint:** `GET /api/appointments/upcoming?page=0&size=10`

### 7. Get Past Appointments

**Endpoint:** `GET /api/appointments/past?page=0&size=10`

### 8. Get Cancelled Appointments

**Endpoint:** `GET /api/appointments/cancelled?page=0&size=10`

### 9. Get Confirmed Appointments

**Endpoint:** `GET /api/appointments/confirmed?page=0&size=10`

### 10. Get Pending Appointments

**Endpoint:** `GET /api/appointments/pending?page=0&size=10`

### 11. Cancel Appointment

**Endpoint:** `PUT /api/appointments/{id}/cancel`

**Response:**
```json
{
  "success": true,
  "message": "Appointment cancelled successfully",
  "data": {
    "id": 1,
    "status": "CANCELLED"
  }
}
```

### 12. Confirm Appointment

**Endpoint:** `PUT /api/appointments/{id}/confirm`

### 13. Reschedule Appointment

**Endpoint:** `PUT /api/appointments/{id}/reschedule`

**Request Body:**
```json
{
  "appointmentDate": "2025-02-20T14:00:00",
  "title": "Rescheduled Consultation"
}
```

### 14. Check Counselor Availability

**Endpoint:** `GET /api/appointments/availability?counselorId=2&dateTime=2025-02-15T10:00:00`

**Response:**
```json
{
  "success": true,
  "message": "Counselor is available",
  "data": true
}
```

### 15. Get Appointment Statistics

**Endpoint:** `GET /api/appointments/stats`

**Response:**
```json
{
  "success": true,
  "data": {
    "totalAppointments": 150,
    "todayAppointments": 8,
    "monthlyAppointments": 45,
    "scheduled": 30,
    "confirmed": 50,
    "completed": 60,
    "cancelled": 10
  }
}
```

### 16. Get Today's Appointments

**Endpoint:** `GET /api/appointments/today?page=0&size=10`

### 17. Export Appointments

**Endpoint:** `GET /api/appointments/export?format=csv&startDate=2025-01-01&endDate=2025-02-28`

**Response:** CSV file download

---

## Frontend Components

### 1. Appointment Service (API Client)

```typescript
// services/appointmentService.ts
import axios from 'axios';
import { authHeader } from './authService';

const API_URL = '/api/appointments';

export const appointmentService = {
  create: async (data: CreateAppointmentRequest) => {
    const response = await axios.post(`${API_URL}`, data, { headers: authHeader() });
    return response.data;
  },

  getAll: async (page = 0, size = 10) => {
    const response = await axios.get(`${API_URL}?page=${page}&size=${size}`, { 
      headers: authHeader() 
    });
    return response.data;
  },

  getById: async (id: number) => {
    const response = await axios.get(`${API_URL}/${id}`, { headers: authHeader() });
    return response.data;
  },

  getByClient: async (clientId: number, page = 0, size = 10) => {
    const response = await axios.get(`${API_URL}/client/${clientId}?page=${page}&size=${size}`, {
      headers: authHeader()
    });
    return response.data;
  },

  getByCounselor: async (counselorId: number, page = 0, size = 10) => {
    const response = await axios.get(`${API_URL}/counselor/${counselorId}?page=${page}&size=${size}`, {
      headers: authHeader()
    });
    return response.data;
  },

  getUpcoming: async (page = 0, size = 10) => {
    const response = await axios.get(`${API_URL}/upcoming?page=${page}&size=${size}`, {
      headers: authHeader()
    });
    return response.data;
  },

  getPast: async (page = 0, size = 10) => {
    const response = await axios.get(`${API_URL}/past?page=${page}&size=${size}`, {
      headers: authHeader()
    });
    return response.data;
  },

  getCancelled: async (page = 0, size = 10) => {
    const response = await axios.get(`${API_URL}/cancelled?page=${page}&size=${size}`, {
      headers: authHeader()
    });
    return response.data;
  },

  getConfirmed: async (page = 0, size = 10) => {
    const response = await axios.get(`${API_URL}/confirmed?page=${page}&size=${size}`, {
      headers: authHeader()
    });
    return response.data;
  },

  getPending: async (page = 0, size = 10) => {
    const response = await axios.get(`${API_URL}/pending?page=${page}&size=${size}`, {
      headers: authHeader()
    });
    return response.data;
  },

  cancel: async (id: number) => {
    const response = await axios.put(`${API_URL}/${id}/cancel`, {}, { headers: authHeader() });
    return response.data;
  },

  confirm: async (id: number) => {
    const response = await axios.put(`${API_URL}/${id}/confirm`, {}, { headers: authHeader() });
    return response.data;
  },

  reschedule: async (id: number, data: RescheduleRequest) => {
    const response = await axios.put(`${API_URL}/${id}/reschedule`, data, { headers: authHeader() });
    return response.data;
  },

  checkAvailability: async (counselorId: number, dateTime: string) => {
    const response = await axios.get(`${API_URL}/availability?counselorId=${counselorId}&dateTime=${dateTime}`, {
      headers: authHeader()
    });
    return response.data;
  },

  getStatistics: async () => {
    const response = await axios.get(`${API_URL}/stats`, { headers: authHeader() });
    return response.data;
  },

  getTodays: async (page = 0, size = 10) => {
    const response = await axios.get(`${API_URL}/today?page=${page}&size=${size}`, {
      headers: authHeader()
    });
    return response.data;
  },

  export: async (format = 'csv', startDate?: string, endDate?: string) => {
    let url = `${API_URL}/export?format=${format}`;
    if (startDate) url += `&startDate=${startDate}`;
    if (endDate) url += `&endDate=${endDate}`;
    
    const response = await axios.get(url, { 
      headers: authHeader(),
      responseType: 'blob'
    });
    return response.data;
  }
};
```

### 2. Appointment Types

```typescript
// types/appointment.ts
export interface Appointment {
  id: number;
  title: string;
  studentId: number;
  studentName: string;
  counselorId: number;
  counselorName: string;
  appointmentDate: string;
  type: AppointmentType;
  status: AppointmentStatus;
  duration: number;
  description?: string;
}

export type AppointmentType = 'INDIVIDUAL' | 'GROUP' | 'COUPLES' | 'FAMILY';

export type AppointmentStatus = 'SCHEDULED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';

export interface CreateAppointmentRequest {
  studentId: number;
  counselorId: number;
  title: string;
  appointmentDate: string;
  type: AppointmentType;
  description?: string;
  duration?: number;
}

export interface RescheduleRequest {
  appointmentDate: string;
  title?: string;
}

export interface AppointmentStatistics {
  totalAppointments: number;
  todayAppointments: number;
  monthlyAppointments: number;
  scheduled: number;
  confirmed: number;
  completed: number;
  cancelled: number;
}
```

### 3. Appointment Form Component

```tsx
// components/AppointmentForm.tsx
import React, { useState, useEffect } from 'react';
import { appointmentService } from '../services/appointmentService';
import { CreateAppointmentRequest, AppointmentType } from '../types/appointment';

interface Props {
  onSuccess: () => void;
  onCancel: () => void;
  initialCounselorId?: number;
}

const AppointmentForm: React.FC<Props> = ({ onSuccess, onCancel, initialCounselorId }) => {
  const [formData, setFormData] = useState<CreateAppointmentRequest>({
    studentId: 0,
    counselorId: initialCounselorId || 0,
    title: '',
    appointmentDate: '',
    type: 'INDIVIDUAL',
    description: '',
    duration: 60
  });
  const [isAvailable, setIsAvailable] = useState<boolean | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const checkAvailability = async () => {
    if (formData.counselorId && formData.appointmentDate) {
      try {
        const response = await appointmentService.checkAvailability(
          formData.counselorId,
          formData.appointmentDate
        );
        setIsAvailable(response.data);
      } catch (err) {
        setError('Failed to check availability');
      }
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      await appointmentService.create(formData);
      onSuccess();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create appointment');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="appointment-form">
      {error && <div className="error">{error}</div>}
      
      <div className="form-group">
        <label>Title</label>
        <input
          type="text"
          value={formData.title}
          onChange={(e) => setFormData({ ...formData, title: e.target.value })}
          required
        />
      </div>

      <div className="form-group">
        <label>Counselor ID</label>
        <input
          type="number"
          value={formData.counselorId}
          onChange={(e) => setFormData({ ...formData, counselorId: parseInt(e.target.value) })}
          required
        />
      </div>

      <div className="form-group">
        <label>Date & Time</label>
        <input
          type="datetime-local"
          value={formData.appointmentDate}
          onChange={(e) => setFormData({ ...formData, appointmentDate: e.target.value })}
          onBlur={checkAvailability}
          required
        />
        {isAvailable !== null && (
          <span className={isAvailable ? 'available' : 'unavailable'}>
            {isAvailable ? '✓ Available' : '✗ Not Available'}
          </span>
        )}
      </div>

      <div className="form-group">
        <label>Type</label>
        <select
          value={formData.type}
          onChange={(e) => setFormData({ ...formData, type: e.target.value as AppointmentType })}
        >
          <option value="INDIVIDUAL">Individual</option>
          <option value="GROUP">Group</option>
          <option value="COUPLES">Couples</option>
          <option value="FAMILY">Family</option>
        </select>
      </div>

      <div className="form-group">
        <label>Duration (minutes)</label>
        <select
          value={formData.duration}
          onChange={(e) => setFormData({ ...formData, duration: parseInt(e.target.value) })}
        >
          <option value={30}>30 minutes</option>
          <option value={45}>45 minutes</option>
          <option value={60}>60 minutes</option>
          <option value={90}>90 minutes</option>
        </select>
      </div>

      <div className="form-group">
        <label>Description</label>
        <textarea
          value={formData.description}
          onChange={(e) => setFormData({ ...formData, description: e.target.value })}
        />
      </div>

      <div className="form-actions">
        <button type="button" onClick={onCancel}>Cancel</button>
        <button type="submit" disabled={loading || (isAvailable === false)}>
          {loading ? 'Creating...' : 'Create Appointment'}
        </button>
      </div>
    </form>
  );
};

export default AppointmentForm;
```

### 4. Appointment List Component

```tsx
// components/AppointmentList.tsx
import React, { useState, useEffect } from 'react';
import { appointmentService } from '../services/appointmentService';
import { Appointment } from '../types/appointment';

interface Props {
  filter?: 'upcoming' | 'past' | 'cancelled' | 'confirmed' | 'pending' | 'today';
  clientId?: number;
  counselorId?: number;
}

const AppointmentList: React.FC<Props> = ({ filter, clientId, counselorId }) => {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadAppointments();
  }, [filter, clientId, counselorId]);

  const loadAppointments = async () => {
    setLoading(true);
    try {
      let response;
      if (clientId) {
        response = await appointmentService.getByClient(clientId);
      } else if (counselorId) {
        response = await appointmentService.getByCounselor(counselorId);
      } else {
        switch (filter) {
          case 'upcoming':
            response = await appointmentService.getUpcoming();
            break;
          case 'past':
            response = await appointmentService.getPast();
            break;
          case 'cancelled':
            response = await appointmentService.getCancelled();
            break;
          case 'confirmed':
            response = await appointmentService.getConfirmed();
            break;
          case 'pending':
            response = await appointmentService.getPending();
            break;
          case 'today':
            response = await appointmentService.getTodays();
            break;
          default:
            response = await appointmentService.getAll();
        }
      }
      setAppointments(response.data?.content || response.data || []);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load appointments');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (id: number) => {
    if (window.confirm('Are you sure you want to cancel this appointment?')) {
      try {
        await appointmentService.cancel(id);
        loadAppointments();
      } catch (err: any) {
        alert(err.response?.data?.message || 'Failed to cancel appointment');
      }
    }
  };

  const handleConfirm = async (id: number) => {
    try {
      await appointmentService.confirm(id);
      loadAppointments();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to confirm appointment');
    }
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="appointment-list">
      <table>
        <thead>
          <tr>
            <th>Title</th>
            <th>Date & Time</th>
            <th>Counselor</th>
            <th>Type</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {appointments.length === 0 ? (
            <tr>
              <td colSpan={6}>No appointments found</td>
            </tr>
          ) : (
            appointments.map((apt) => (
              <tr key={apt.id}>
                <td>{apt.title}</td>
                <td>{new Date(apt.appointmentDate).toLocaleString()}</td>
                <td>{apt.counselorName}</td>
                <td>{apt.type}</td>
                <td>
                  <span className={`status ${apt.status.toLowerCase()}`}>
                    {apt.status}
                  </span>
                </td>
                <td>
                  {apt.status === 'SCHEDULED' && (
                    <>
                      <button onClick={() => handleConfirm(apt.id)}>Confirm</button>
                      <button onClick={() => handleCancel(apt.id)}>Cancel</button>
                    </>
                  )}
                  {apt.status === 'CONFIRMED' && (
                    <button onClick={() => handleCancel(apt.id)}>Cancel</button>
                  )}
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
};

export default AppointmentList;
```

### 5. Appointment Statistics Component

```tsx
// components/AppointmentStatistics.tsx
import React, { useState, useEffect } from 'react';
import { appointmentService } from '../services/appointmentService';
import { AppointmentStatistics } from '../types/appointment';

const AppointmentStatistics: React.FC = () => {
  const [stats, setStats] = useState<AppointmentStatistics | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadStatistics();
  }, []);

  const loadStatistics = async () => {
    try {
      const response = await appointmentService.getStatistics();
      setStats(response.data);
    } catch (err) {
      console.error('Failed to load statistics', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading statistics...</div>;
  if (!stats) return <div>Failed to load statistics</div>;

  return (
    <div className="statistics-grid">
      <div className="stat-card">
        <h3>Total Appointments</h3>
        <p>{stats.totalAppointments}</p>
      </div>
      <div className="stat-card">
        <h3>Today's Appointments</h3>
        <p>{stats.todayAppointments}</p>
      </div>
      <div className="stat-card">
        <h3>Monthly Appointments</h3>
        <p>{stats.monthlyAppointments}</p>
      </div>
      <div className="stat-card">
        <h3>Scheduled</h3>
        <p>{stats.scheduled}</p>
      </div>
      <div className="stat-card">
        <h3>Confirmed</h3>
        <p>{stats.confirmed}</p>
      </div>
      <div className="stat-card">
        <h3>Completed</h3>
        <p>{stats.completed}</p>
      </div>
      <div className="stat-card">
        <h3>Cancelled</h3>
        <p>{stats.cancelled}</p>
      </div>
    </div>
  );
};

export default AppointmentStatistics;
```

### 6. Calendar View Component

```tsx
// components/AppointmentCalendar.tsx
import React, { useState, useEffect } from 'react';
import { appointmentService } from '../services/appointmentService';
import { Appointment } from '../types/appointment';

const AppointmentCalendar: React.FC = () => {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [currentDate, setCurrentDate] = useState(new Date());

  useEffect(() => {
    loadMonthAppointments();
  }, [currentDate]);

  const loadMonthAppointments = async () => {
    const startOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
    const endOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0);
    
    try {
      const response = await appointmentService.getAll(0, 100);
      const allAppointments = response.data?.content || response.data || [];
      
      // Filter to current month
      const filtered = allAppointments.filter((apt: Appointment) => {
        const aptDate = new Date(apt.appointmentDate);
        return aptDate >= startOfMonth && aptDate <= endOfMonth;
      });
      
      setAppointments(filtered);
    } catch (err) {
      console.error('Failed to load appointments', err);
    }
  };

  const getDaysInMonth = () => {
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();
    const days = new Date(year, month + 1, 0).getDate();
    const firstDay = new Date(year, month, 1).getDay();
    
    const result = [];
    for (let i = 0; i < firstDay; i++) {
      result.push(null);
    }
    for (let i = 1; i <= days; i++) {
      result.push(i);
    }
    return result;
  };

  const getAppointmentsForDay = (day: number) => {
    return appointments.filter((apt) => {
      const aptDate = new Date(apt.appointmentDate);
      return aptDate.getDate() === day && 
             aptDate.getMonth() === currentDate.getMonth() &&
             aptDate.getFullYear() === currentDate.getFullYear();
    });
  };

  const days = getDaysInMonth();

  return (
    <div className="calendar">
      <div className="calendar-header">
        <button onClick={() => setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1))}>
          ← Previous
        </button>
        <h3>
          {currentDate.toLocaleString('default', { month: 'long', year: 'numeric' })}
        </h3>
        <button onClick={() => setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1))}>
          Next →
        </button>
      </div>
      
      <div className="calendar-grid">
        {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map((day) => (
          <div key={day} className="calendar-day-header">{day}</div>
        ))}
        
        {days.map((day, index) => (
          <div key={index} className={`calendar-day ${day ? '' : 'empty'}`}>
            {day && (
              <>
                <span className="day-number">{day}</span>
                <div className="day-appointments">
                  {getAppointmentsForDay(day).slice(0, 3).map((apt) => (
                    <div key={apt.id} className={`appointment-dot ${apt.status.toLowerCase()}`}>
                      {new Date(apt.appointmentDate).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} - {apt.title}
                    </div>
                  ))}
                  {getAppointmentsForDay(day).length > 3 && (
                    <div className="more-appointments">
                      +{getAppointmentsForDay(day).length - 3} more
                    </div>
                  )}
                </div>
              </>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default AppointmentCalendar;
```

---

## Authentication

All endpoints require a valid JWT token. Include the token in the Authorization header:

```typescript
const authHeader = () => {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
};
```

---

## Error Handling

All endpoints return a standard response format:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

Handle errors accordingly:

```typescript
try {
  const response = await appointmentService.create(data);
  if (response.success) {
    // Success handling
  } else {
    // Show error message
    alert(response.message);
  }
} catch (error) {
  // Network/server error handling
}
```

---

## Status Flow

```
SCHEDULED → CONFIRMED → COMPLETED
    ↓           ↓
  CANCELLED   CANCELLED
```

---

## Appointment Types

- `INDIVIDUAL` - One-on-one counseling session
- `GROUP` - Group therapy session
- `COUPLES` - Couples counseling
- `FAMILY` - Family counseling session

---

## Complete API Endpoints Reference

### Core Appointment Endpoints

| # | Method | Endpoint | Description | Roles |
|---|--------|----------|-------------|-------|
| 1 | POST | `/api/appointments` | Create a new appointment | ADMIN, COUNSELOR, STUDENT, CLIENT |
| 2 | GET | `/api/appointments` | Get all appointments (paginated) | All authenticated |
| 3 | GET | `/api/appointments/{id}` | Get appointment by ID | All authenticated |
| 4 | GET | `/api/appointments/client/{clientId}` | Get appointments by client | All authenticated |
| 5 | GET | `/api/appointments/counselor/{counselorId}` | Get appointments by counselor | All authenticated |
| 6 | GET | `/api/appointments/upcoming` | Get upcoming appointments | ADMIN, COUNSELOR, STUDENT, CLIENT |
| 7 | GET | `/api/appointments/past` | Get past appointments | ADMIN, COUNSELOR, STUDENT, CLIENT |
| 8 | GET | `/api/appointments/cancelled` | Get cancelled appointments | ADMIN, COUNSELOR |
| 9 | GET | `/api/appointments/confirmed` | Get confirmed appointments | ADMIN, COUNSELOR, STUDENT, CLIENT |
| 10 | GET | `/api/appointments/pending` | Get pending appointments | ADMIN, COUNSELOR, STUDENT, CLIENT |
| 11 | PUT | `/api/appointments/{id}/cancel` | Cancel an appointment | ADMIN, COUNSELOR, STUDENT, CLIENT |
| 12 | PUT | `/api/appointments/{id}/confirm` | Confirm an appointment | ADMIN, COUNSELOR |
| 13 | PUT | `/api/appointments/{id}/reschedule` | Reschedule an appointment | ADMIN, COUNSELOR, STUDENT, CLIENT |
| 14 | GET | `/api/appointments/availability` | Check counselor availability | ADMIN, COUNSELOR, STUDENT, CLIENT |
| 15 | GET | `/api/appointments/stats` | Get appointment statistics | ADMIN, COUNSELOR |
| 16 | GET | `/api/appointments/today` | Get today's appointments | ADMIN, COUNSELOR, STUDENT, CLIENT |
| 17 | GET | `/api/appointments/export` | Export appointments (CSV/PDF) | ADMIN |

### Session Assignment Endpoints

| # | Method | Endpoint | Description | Roles |
|---|--------|----------|-------------|-------|
| 18 | GET | `/api/appointments/unassigned` | Get unassigned appointments | ADMIN, COUNSELOR |
| 19 | GET | `/api/appointments/unassigned/count` | Count unassigned appointments | ADMIN, COUNSELOR |
| 20 | POST | `/api/appointments/admin/assign` | Admin assigns session to counselor | ADMIN |
| 21 | POST | `/api/appointments/counselor/take/{appointmentId}` | Counselor takes an appointment | COUNSELOR |

---

## Detailed Endpoint Implementation

### 18. Get Unassigned Appointments

**Endpoint:** `GET /api/appointments/unassigned?page=0&size=10`

**Description:** Retrieves appointments that haven't been assigned to any counselor yet. Useful for counselors to see available appointments they can take.

**Response:**
```json
{
  "success": true,
  "message": "Unassigned appointments retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Initial Consultation",
        "studentId": 5,
        "studentName": "John Doe",
        "counselorId": null,
        "counselorName": "Unassigned",
        "appointmentDate": "2025-02-20T10:00:00",
        "type": "INDIVIDUAL",
        "status": "SCHEDULED",
        "duration": 60,
        "description": "First session for academic stress"
      }
    ],
    "totalElements": 3,
    "totalPages": 1,
    "page": 0,
    "size": 10
  }
}
```

**Frontend Service:**
```typescript
getUnassigned: async (page = 0, size = 10) => {
  const response = await axios.get(`${API_URL}/unassigned?page=${page}&size=${size}`, {
    headers: authHeader()
  });
  return response.data;
}
```

**Frontend Component Usage:**
```tsx
// UnassignedAppointments.tsx
const UnassignedAppointments: React.FC = () => {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadUnassigned();
  }, []);

  const loadUnassigned = async () => {
    try {
      const response = await appointmentService.getUnassigned();
      setAppointments(response.data?.content || []);
    } catch (err) {
      console.error('Failed to load unassigned appointments', err);
    } finally {
      setLoading(false);
    }
  };

  const handleTakeAppointment = async (appointmentId: number, counselorId: number) => {
    try {
      await appointmentService.counselorTakeAppointment(appointmentId, counselorId);
      loadUnassigned(); // Refresh list
    } catch (err) {
      alert('Failed to take appointment');
    }
  };

  return (
    <div className="unassigned-appointments">
      <h2>Available Appointments</h2>
      {appointments.map(apt => (
        <div key={apt.id} className="appointment-card">
          <h3>{apt.title}</h3>
          <p>Student: {apt.studentName}</p>
          <p>Date: {new Date(apt.appointmentDate).toLocaleString()}</p>
          <button onClick={() => handleTakeAppointment(apt.id, currentCounselorId)}>
            Take Appointment
          </button>
        </div>
      ))}
    </div>
  );
};
```

### 19. Count Unassigned Appointments

**Endpoint:** `GET /api/appointments/unassigned/count`

**Description:** Returns the total count of appointments waiting to be assigned. Useful for dashboard badges/notifications.

**Response:**
```json
{
  "success": true,
  "message": "Unassigned appointments count",
  "data": 5
}
```

**Frontend Service:**
```typescript
countUnassigned: async () => {
  const response = await axios.get(`${API_URL}/unassigned/count`, {
    headers: authHeader()
  });
  return response.data;
}
```

**Frontend Component Usage:**
```tsx
// DashboardHeader.tsx
const DashboardHeader: React.FC = () => {
  const [unassignedCount, setUnassignedCount] = useState(0);

  useEffect(() => {
    const loadCount = async () => {
      try {
        const response = await appointmentService.countUnassigned();
        setUnassignedCount(response.data);
      } catch (err) {
        console.error('Failed to load count', err);
      }
    };
    loadCount();
    // Poll every 30 seconds
    const interval = setInterval(loadCount, 30000);
    return () => clearInterval(interval);
  }, []);

  return (
    <header>
      <h1>Counseling Dashboard</h1>
      {unassignedCount > 0 && (
        <span className="badge">{unassignedCount} unassigned</span>
      )}
    </header>
  );
};
```

### 20. Admin Assign Session to Counselor

**Endpoint:** `POST /api/appointments/admin/assign`

**Description:** Admin manually assigns an unassigned appointment to a specific counselor.

**Request Body:**
```json
{
  "appointmentId": 1,
  "counselorId": 5,
  "notes": "Assigned based on specialty in anxiety disorders"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Session assigned to counselor successfully",
  "data": {
    "id": 1,
    "counselorId": 5,
    "counselorName": "Dr. Smith",
    "status": "SCHEDULED"
  }
}
```

**Frontend Service:**
```typescript
assignToCounselor: async (appointmentId: number, counselorId: number, notes?: string) => {
  const response = await axios.post(`${API_URL}/admin/assign`, 
    { appointmentId, counselorId, notes },
    { headers: authHeader() }
  );
  return response.data;
}
```

**Frontend Component Usage:**
```tsx
// AssignCounselorModal.tsx
interface Props {
  appointmentId: number;
  onClose: () => void;
  onAssign: () => void;
}

const AssignCounselorModal: React.FC<Props> = ({ appointmentId, onClose, onAssign }) => {
  const [counselors, setCounselors] = useState<Counselor[]>([]);
  const [selectedCounselor, setSelectedCounselor] = useState<number | null>(null);
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadCounselors();
  }, []);

  const loadCounselors = async () => {
    const response = await counselorService.getAll();
    setCounselors(response.data);
  };

  const handleAssign = async () => {
    if (!selectedCounselor) return;
    setLoading(true);
    try {
      await appointmentService.assignToCounselor(appointmentId, selectedCounselor, notes);
      onAssign();
    } catch (err) {
      alert('Failed to assign counselor');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal">
        <h2>Assign Counselor</h2>
        <select onChange={(e) => setSelectedCounselor(Number(e.target.value))}>
          <option value="">Select counselor...</option>
          {counselors.map(c => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
        <textarea
          placeholder="Optional notes..."
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
        />
        <div className="actions">
          <button onClick={onClose}>Cancel</button>
          <button onClick={handleAssign} disabled={!selectedCounselor || loading}>
            {loading ? 'Assigning...' : 'Assign'}
          </button>
        </div>
      </div>
    </div>
  );
};
```

### 21. Counselor Take Appointment

**Endpoint:** `POST /api/appointments/counselor/take/{appointmentId}?counselorId={id}`

**Description:** Counselor claims an unassigned appointment for themselves.

**Response:**
```json
{
  "success": true,
  "message": "Appointment taken successfully",
  "data": {
    "id": 1,
    "counselorId": 5,
    "counselorName": "Dr. Smith",
    "status": "SCHEDULED"
  }
}
```

**Frontend Service:**
```typescript
counselorTakeAppointment: async (appointmentId: number, counselorId: number) => {
  const response = await axios.post(
    `${API_URL}/counselor/take/${appointmentId}?counselorId=${counselorId}`,
    {},
    { headers: authHeader() }
  );
  return response.data;
}
```

**Frontend Component Usage:**
```tsx
// AvailableAppointmentsCard.tsx
interface Props {
  appointment: Appointment;
  counselorId: number;
  onTaken: () => void;
}

const AvailableAppointmentsCard: React.FC<Props> = ({ appointment, counselorId, onTaken }) => {
  const [taking, setTaking] = useState(false);

  const handleTake = async () => {
    if (!window.confirm(`Take this appointment for ${appointment.studentName}?`)) return;
    setTaking(true);
    try {
      await appointmentService.counselorTakeAppointment(appointment.id, counselorId);
      onTaken();
    } catch (err) {
      alert('Failed to take appointment');
    } finally {
      setTaking(false);
    }
  };

  return (
    <div className="appointment-card">
      <h3>{appointment.title}</h3>
      <p><strong>Student:</strong> {appointment.studentName}</p>
      <p><strong>Date:</strong> {new Date(appointment.appointmentDate).toLocaleString()}</p>
      <p><strong>Type:</strong> {appointment.type}</p>
      <p><strong>Description:</strong> {appointment.description}</p>
      <button onClick={handleTake} disabled={taking}>
        {taking ? 'Taking...' : 'Take Appointment'}
      </button>
    </div>
  );
};
```

---

## Updated Appointment Service (Complete)

```typescript
// services/appointmentService.ts
import axios from 'axios';
import { authHeader } from './authService';

const API_URL = '/api/appointments';

export const appointmentService = {
  // CRUD Operations
  create: async (data: CreateAppointmentRequest) => {
    const response = await axios.post(`${API_URL}`, data, { headers: authHeader() });
    return response.data;
  },

  getAll: async (page = 0, size = 10) => {
    const response = await axios.get(`${API_URL}?page=${page}&size=${size}`, { 
      headers: authHeader() 
    });
    return response.data;
  },

  getById: async (id: number) => {
    const response = await axios.get(`${API_URL}/${id}`, { headers: authHeader() });
    return response.data;
  },

  // Filter by entity
  getByClient: async (clientId: number, page = 0, size = 10) => {
    const response = await axios.get(`${API_URL}/client/${clientId}?page=${page}&size=${size}`, {
      headers: authHeader()
    });
    return response.data;
  },

  getByCounselor: async (counselorId: number, page = 0, size = 10) => {
    const response = await axios.get(`${API_URL}/counselor/${counselorId}?page=${page}&size=${size}`, {
      headers: authHeader()
    });
    return response.data;
  },

  // Status filters
  getUpcoming: async (page = 0, size = 10) => {
    const response = await axios.get(`${API_URL}/upcoming?page=${page}&size=${size}`, {
      headers: authHeader()
    });
    return response.data;
  },

  getPast: async (page = 0, size = 10) => {
    const response = await axios.get(`${API_URL}/past?page=${page}&size=${size}`, {
      headers: authHeader()
    });
    return response.data;
  },

  getCancelled: async (page = 0, size = 10) => {
    const response = await axios.get(`${API_URL}/cancelled?page=${page}&size=${size}`, {
      headers: authHeader()
    });
    return response.data;
  },

  getConfirmed: async (page = 0, size = 10) => {
    const response = await axios.get(`${API_URL}/confirmed?page=${page}&size=${size}`, {
      headers: authHeader()
    });
    return response.data;
  },

  getPending: async (page = 0, size = 10) => {
    const response = await axios.get(`${API_URL}/pending?page=${page}&size=${size}`, {
      headers: authHeader()
    });
    return response.data;
  },

  getTodays: async (page = 0, size = 10) => {
    const response = await axios.get(`${API_URL}/today?page=${page}&size=${size}`, {
      headers: authHeader()
    });
    return response.data;
  },

  // Actions
  cancel: async (id: number) => {
    const response = await axios.put(`${API_URL}/${id}/cancel`, {}, { headers: authHeader() });
    return response.data;
  },

  confirm: async (id: number) => {
    const response = await axios.put(`${API_URL}/${id}/confirm`, {}, { headers: authHeader() });
    return response.data;
  },

  reschedule: async (id: number, data: RescheduleRequest) => {
    const response = await axios.put(`${API_URL}/${id}/reschedule`, data, { headers: authHeader() });
    return response.data;
  },

  // Availability
  checkAvailability: async (counselorId: number, dateTime: string) => {
    const response = await axios.get(
      `${API_URL}/availability?counselorId=${counselorId}&dateTime=${dateTime}`,
      { headers: authHeader() }
    );
    return response.data;
  },

  // Statistics
  getStatistics: async () => {
    const response = await axios.get(`${API_URL}/stats`, { headers: authHeader() });
    return response.data;
  },

  // Export
  export: async (format = 'csv', startDate?: string, endDate?: string) => {
    let url = `${API_URL}/export?format=${format}`;
    if (startDate) url += `&startDate=${startDate}`;
    if (endDate) url += `&endDate=${endDate}`;
    
    const response = await axios.get(url, { 
      headers: authHeader(),
      responseType: 'blob'
    });
    return response.data;
  },

  // Session Assignment (New)
  getUnassigned: async (page = 0, size = 10) => {
    const response = await axios.get(`${API_URL}/unassigned?page=${page}&size=${size}`, {
      headers: authHeader()
    });
    return response.data;
  },

  countUnassigned: async () => {
    const response = await axios.get(`${API_URL}/unassigned/count`, {
      headers: authHeader()
    });
    return response.data;
  },

  assignToCounselor: async (appointmentId: number, counselorId: number, notes?: string) => {
    const response = await axios.post(`${API_URL}/admin/assign`, 
      { appointmentId, counselorId, notes },
      { headers: authHeader() }
    );
    return response.data;
  },

  counselorTakeAppointment: async (appointmentId: number, counselorId: number) => {
    const response = await axios.post(
      `${API_URL}/counselor/take/${appointmentId}?counselorId=${counselorId}`,
      {},
      { headers: authHeader() }
    );
    return response.data;
  }
};
```

---

## Updated TypeScript Types

```typescript
// types/appointment.ts
export interface Appointment {
  id: number;
  title: string;
  studentId: number;
  studentName: string;
  counselorId: number | null;
  counselorName: string;
  appointmentDate: string;
  type: AppointmentType;
  status: AppointmentStatus;
  duration: number;
  description?: string;
  meetingLink?: string;
  location?: string;
  createdAt?: string;
}

export type AppointmentType = 'INDIVIDUAL' | 'GROUP' | 'COUPLES' | 'FAMILY';

export type AppointmentStatus = 'SCHEDULED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW';

export interface CreateAppointmentRequest {
  studentId: number;
  counselorId?: number; // Optional - can be assigned later
  title: string;
  appointmentDate: string;
  type: AppointmentType;
  description?: string;
  duration?: number;
}

export interface RescheduleRequest {
  appointmentDate: string;
  title?: string;
}

export interface AssignAppointmentRequest {
  appointmentId: number;
  counselorId: number;
  notes?: string;
}

export interface AppointmentStatistics {
  totalAppointments: number;
  todayAppointments: number;
  monthlyAppointments: number;
  scheduled: number;
  confirmed: number;
  completed: number;
  cancelled: number;
}
```
