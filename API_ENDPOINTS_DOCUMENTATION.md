# UNZA Counseling Management System - API Documentation

**Base URL:** `https://your-backend-domain.com/api/v1`  
**WebSocket URL:** `wss://your-backend-domain.com/ws`

---

## 1. AUTHENTICATION ENDPOINTS

### POST `/auth/login`
**Description:** Authenticate user with email/password  
**Auth:** None  
**Content-Type:** application/json  

**Request Body:**
```json
{
  "identifier": "user@example.com",
  "password": "userpassword"
}
```

**Success Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh-token-string",
  "user": {
    "id": 1,
    "username": "john.doe",
    "email": "john.doe@unza.zm",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+260123456789",
    "profilePicture": "https://...",
    "bio": "Student bio",
    "gender": "MALE",
    "dateOfBirth": "2000-01-15T00:00:00",
    "department": "Computer Science",
    "program": "BSc ICT",
    "yearOfStudy": 3,
    "studentId": "2020123456",
    "active": true,
    "emailVerified": true,
    "twoFactorEnabled": false,
    "roles": ["ROLE_STUDENT"],
    "createdAt": "2024-01-01T10:00:00",
    "lastLogin": "2024-06-17T05:00:00"
  },
  "expiresIn": 3600000,
  "requiresConsent": false,
  "isFirstLogin": false
}
```

### POST `/auth/register`
**Description:** Register new user  
**Auth:** None  

**Request Body:**
```json
{
  "username": "john.doe",
  "email": "john.doe@unza.zm",
  "password": "securepassword123",
  "firstName": "John",
  "lastName": "Doe",
  "studentId": "2020123456",
  "phoneNumber": "+260123456789",
  "department": "Computer Science",
  "program": "BSc ICT",
  "yearOfStudy": 3
}
```

### POST `/auth/logout`
**Description:** Logout (client-side token removal for JWT)  
**Auth:** Bearer Token  

**Response (200):**
```json
{
  "message": "Logout successful"
}
```

### POST `/auth/refresh`
**Description:** Refresh JWT token  
**Auth:** None  

**Request Body:**
```json
{
  "refreshToken": "refresh-token-string"
}
```

### POST `/auth/anonymous-login`
**Description:** Anonymous access login  
**Auth:** None  

**Request Body:**
```json
{
  "ipAddress": "192.168.1.1",
  "userAgent": "Mozilla/5.0..."
}
```

---

## 2. USER ENDPOINTS

### GET `/users`
**Description:** Get all users (paginated)  
**Auth:** ADMIN, COUNSELOR  

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| page | int | No (default: 0) | Page number |
| size | int | No (default: 20) | Page size |

**Success Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "username": "john.doe",
      "email": "john.doe@unza.zm",
      "firstName": "John",
      "lastName": "Doe"
    }
  ],
  "pageable": { ... },
  "totalElements": 100,
  "totalPages": 5
}
```

### GET `/users/profile`
**Description:** Get current user profile  
**Auth:** Authenticated  

**Success Response (200):**
```json
{
  "id": 1,
  "username": "john.doe",
  "email": "john.doe@unza.zm",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+260123456789",
  "profilePicture": "https://...",
  "bio": "...",
  "gender": "MALE",
  "dateOfBirth": "2000-01-15T00:00:00",
  "department": "Computer Science",
  "program": "BSc ICT",
  "yearOfStudy": 3,
  "studentId": "2020123456",
  "active": true,
  "emailVerified": true,
  "twoFactorEnabled": false,
  "roles": ["ROLE_STUDENT"]
}
```

---

## 3. CLIENT ENDPOINTS

### POST `/clients`
**Description:** Create new client  
**Auth:** Authenticated  

**Request Body:**
```json
{
  "userId": 1,
  "studentId": "2020123456",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@unza.zm",
  "phoneNumber": "+260123456789",
  "dateOfBirth": "2000-01-15",
  "gender": "MALE",
  "program": "BSc ICT",
  "yearOfStudy": 3,
  "department": "Computer Science",
  "faculty": "ICT",
  "academicStanding": "GOOD",
  "gpa": 3.5,
  "totalSessions": 0,
  "riskLevel": "LOW",
  "riskScore": 10
}
```

### GET `/clients`
**Description:** Get all clients  
**Auth:** ADMIN, COUNSELOR, STUDENT  

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| page | int | No (default: 0) | Page number |
| size | int | No (default: 10) | Page size |
| search | string | No | Search term |
| status | string | No | Client status filter |
| riskLevel | string | No | Risk level filter (LOW, MEDIUM, HIGH, CRITICAL) |

**Success Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "studentId": "2020123456",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@unza.zm",
      "phoneNumber": "+260123456789",
      "status": "ACTIVE",
      "riskLevel": "LOW",
      "gpa": 3.5,
      "totalSessions": 5
    }
  ],
  "totalElements": 50
}
```

---

## 4. COUNSELOR ENDPOINTS

### GET `/counselors`
**Description:** Get all counselors  
**Auth:** Authenticated  

**Success Response (200):**
```json
{
  "data": [
    {
      "id": 1,
      "firstName": "Jane",
      "lastName": "Smith",
      "email": "jane.smith@unza.zm",
      "specialization": "Clinical Psychology",
      "licenseNumber": "CP/2020/001",
      "yearsOfExperience": 5,
      "availableForAppointments": true
    }
  ]
}
```

---

## 5. APPOINTMENT ENDPOINTS

### GET `/appointments`
**Description:** Get all appointments  
**Auth:** Authenticated  

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| page | int | No (default: 0) | Page number |
| size | int | No (default: 20) | Page size |

**Success Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Initial Consultation",
      "studentId": 1,
      "studentName": "John Doe",
      "counselorId": 2,
      "counselorName": "Jane Smith",
      "appointmentDate": "2024-06-20T14:00:00",
      "duration": 60,
      "type": "INDIVIDUAL",
      "status": "PENDING",
      "sessionMode": "IN_PERSON",
      "urgencyLevel": "NORMAL",
      "description": "First counseling session",
      "location": "Room 101"
    }
  ],
  "totalElements": 25
}
```

### POST `/appointments`
**Description:** Create appointment  
**Auth:** ADMIN, COUNSELOR, STUDENT, CLIENT  

**Request Body:**
```json
{
  "studentId": "2020123456",
  "counselorId": 2,
  "appointmentDate": "2024-06-20T14:00:00",
  "duration": 60,
  "type": "INDIVIDUAL",
  "sessionMode": "IN_PERSON",
  "urgencyLevel": "NORMAL",
  "description": "Follow-up session",
  "location": "Room 101",
  "presentingConcern": "Anxiety",
  "consentAcknowledged": true
}
```

---

## 6. CASE ENDPOINTS

### POST `/cases`
**Description:** Create case  
**Auth:** ADMIN, COUNSELOR  

**Request Body:**
```json
{
  "clientId": 1,
  "counselorId": 2,
  "subject": "Anxiety Management",
  "description": "Student experiencing anxiety issues",
  "priority": "MEDIUM",
  "status": "OPEN",
  "assignedDate": "2024-06-17T00:00:00"
}
```

### GET `/cases`
**Description:** Get all cases  
**Auth:** ADMIN, COUNSELOR  

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| status | string | No | CaseStatus (OPEN, IN_PROGRESS, CLOSED, ARCHIVED) |
| priority | string | No | CasePriority (LOW, MEDIUM, HIGH, CRITICAL) |
| counselorId | long | No | Filter by counselor |
| clientId | long | No | Filter by client |

**Success Response (200):**
```json
[
  {
    "id": 1,
    "caseNumber": "CASE-2024-0001",
    "clientId": 1,
    "clientName": "John Doe",
    "counselorId": 2,
    "counselorName": "Jane Smith",
    "subject": "Anxiety Management",
    "description": "Student experiencing anxiety issues",
    "status": "OPEN",
    "priority": "MEDIUM",
    "createdAt": "2024-06-17T10:00:00",
    "updatedAt": "2024-06-17T10:00:00"
  }
]
```

---

## 7. MESSAGE ENDPOINTS

### GET `/messages`
**Description:** Get all messages for user  
**Auth:** Authenticated  

**Success Response (200):**
```json
[
  {
    "id": 1,
    "senderId": 1,
    "recipientId": 2,
    "subject": "Session Follow-up",
    "content": "How are you feeling today?",
    "status": "SENT",
    "isRead": false,
    "createdAt": "2024-06-17T10:00:00"
  }
]
```

### POST `/messages/send`
**Description:** Send message  
**Auth:** Authenticated  

**Request Body:**
```json
{
  "recipientId": 2,
  "subject": "Session Follow-up",
  "content": "How are you feeling today?"
}
```

---

## 8. CONVERSATION ENDPOINTS

### GET `/conversations`
**Description:** Get user conversations  
**Auth:** Authenticated  

**Success Response (200):**
```json
[
  {
    "conversationId": 1,
    "partnerId": 2,
    "partnerUsername": "jane.smith",
    "partnerEmail": "jane.smith@unza.zm",
    "partnerFirstName": "Jane",
    "partnerLastName": "Smith",
    "partnerFullName": "Jane Smith",
    "partnerProfilePicture": "https://...",
    "partnerType": "COUNSELOR",
    "partnerSpecialization": "Clinical Psychology",
    "lastMessageContent": "How are you feeling?",
    "lastMessageTime": "2024-06-17T10:00:00",
    "unreadCount": 2,
    "isOnline": true,
    "lastSeen": "2024-06-17T09:30:00"
  }
]
```

---

## 9. CONSENT FORM ENDPOINTS

### GET `/consent/forms/latest`
**Description:** Get latest active consent form  
**Auth:** None  

**Success Response (200):**
```json
{
  "id": 1,
  "title": "Counseling Consent Form",
  "content": "Full consent form content...",
  "version": "1.0",
  "isActive": true,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

### POST `/consent/sign`
**Description:** Sign consent form  
**Auth:** Authenticated  

**Request Body:**
```json
{
  "consentFormId": 1,
  "consentText": "I agree to the terms...",
  "signatureImage": "base64-encoded-image"
}
```

---

## 10. ASSESSMENT ENDPOINTS

### GET `/self-assessments`
**Description:** Get self-assessments  
**Auth:** STUDENT, CLIENT, COUNSELOR, ADMIN  

**Success Response (200):**
```json
[
  {
    "id": 1,
    "userId": 1,
    "type": "DEPRESSION",
    "score": 15,
    "maxScore": 30,
    "riskLevel": "MEDIUM",
    "responses": {...},
    "createdAt": "2024-06-17T10:00:00"
  }
]
```

### POST `/self-assessments/submit`
**Description:** Submit self-assessment  
**Auth:** STUDENT, CLIENT  

**Request Body:**
```json
{
  "type": "DEPRESSION",
  "answers": {
    "question1": 3,
    "question2": 2,
    "question3": 4
  }
}
```

---

## 11. RESOURCE ENDPOINTS

### GET `/resources`
**Description:** Get all resources  
**Auth:** None  

**Success Response (200):**
```json
[
  {
    "id": 1,
    "title": "Coping with Anxiety",
    "description": "Guide on managing anxiety",
    "type": "PDF",
    "category": "MENTAL_HEALTH",
    "url": "https://...",
    "fileSize": "2MB",
    "downloadCount": 150,
    "featured": true
  }
]
```

---

## 12. GOAL ENDPOINTS

### GET `/goals/client/{clientId}`
**Description:** Get goals by client  
**Auth:** ADMIN, COUNSELOR, CLIENT  

**Success Response (200):**
```json
[
  {
    "id": 1,
    "clientId": 1,
    "title": "Reduce anxiety attacks",
    "description": "From daily to weekly",
    "category": "MENTAL_HEALTH",
    "targetValue": 10,
    "currentValue": 5,
    "unit": "times per week",
    "startDate": "2024-06-01",
    "targetDate": "2024-07-01",
    "status": "IN_PROGRESS",
    "priority": "HIGH"
  }
]
```

---

## 13. CRISIS ALERT ENDPOINTS

### GET `/crisis-alerts`
**Description:** List crisis alerts  
**Auth:** ADMIN, COUNSELOR, SUPER_ADMIN  

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| page | int | No (default: 0) | Page number |
| status | string | No | AlertStatus (PENDING, ACKNOWLEDGED, RESOLVED, FALSE_POSITIVE) |

**Success Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "clientId": 1,
      "clientName": "John Doe",
      "messageContent": "I'm having suicidal thoughts",
      "riskLevel": "CRITICAL",
      "status": "PENDING",
      "createdAt": "2024-06-17T10:00:00"
    }
  ]
}
```

---

## 14. NOTIFICATION ENDPOINTS

### GET `/notifications`
**Description:** Get user notifications  
**Auth:** Authenticated  

**Success Response (200):**
```json
[
  {
    "id": 1,
    "userId": 1,
    "title": "Appointment Reminder",
    "message": "You have a session in 2 hours",
    "type": "APPOINTMENT",
    "isRead": false,
    "createdAt": "2024-06-17T08:00:00"
  }
]
```

---

## 15. DASHBOARD ENDPOINTS

### GET `/dashboard/stats`
**Description:** Get dashboard statistics  
**Auth:** Authenticated  

**Success Response (200):**
```json
{
  "totalClients": 150,
  "activeClients": 120,
  "highRiskClients": 15,
  "totalAppointments": 500,
  "upcomingAppointments": 25,
  "pendingAppointments": 10,
  "avgSessionsPerClient": 4,
  "monthlyAppointments": 120
}
```

---

## WEBSOCKET MESSAGES

### Send Message
**Destination:** `/app/message.send/{recipientId}`  
**Payload:**
```json
{
  "content": "Hello!",
  "subject": "Quick question"
}
```

### Typing Indicator
**Destination:** `/app/typing.start/{partnerId}`  
**Payload:**
```json
{}
```

### Subscribe to Messages
**Destination:** `/user/{userId}/queue/messages`

### Subscribe to Notifications
**Destination:** `/user/{userId}/queue/notifications`

---

## COMMON RESPONSE STRUCTURES

### Paginated Response:
```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 100,
  "totalPages": 5
}
```

### Error Response:
```json
{
  "error": "Error message",
  "timestamp": "2024-06-17T05:00:00",
  "status": 400
}
```

### Success Response Wrapper:
```json
{
  "data": {...},
  "message": "Success message",
  "success": true
}
```

---

## ENUM VALUES

### AppointmentType:
- INDIVIDUAL, COUPLE, FAMILY, GROUP

### AppointmentStatus:
- PENDING, CONFIRMED, COMPLETED, CANCELLED, RESCHEDULED, NO_SHOW

### SessionMode:
- IN_PERSON, VIDEO, PHONE

### UrgencyLevel:
- LOW, NORMAL, HIGH, CRITICAL

### Client.RiskLevel:
- LOW, MEDIUM, HIGH, CRITICAL

### CaseStatus:
- OPEN, IN_PROGRESS, CLOSED, ARCHIVED

### CasePriority:
- LOW, MEDIUM, HIGH, CRITICAL

### Gender:
- MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY

### GoalStatus:
- NOT_STARTED, IN_PROGRESS, COMPLETED, PAUSED, CANCELLED

### GoalCategory:
- ACADEMIC, PERSONAL, RELATIONAL, MENTAL_HEALTH, PHYSICAL_HEALTH, CAREER