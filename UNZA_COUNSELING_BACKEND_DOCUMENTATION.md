# UNZA Counseling Management System - Complete Backend Documentation

## Table of Contents

1. [System Overview](#system-overview)
2. [Architecture & Design](#architecture--design)
3. [Technology Stack](#technology-stack)
4. [API Documentation](#api-documentation)
5. [Database Schema](#database-schema)
6. [Security Implementation](#security-implementation)
7. [Configuration Management](#configuration-management)
8. [Monitoring & Observability](#monitoring--observability)
9. [Deployment Guide](#deployment-guide)
10. [Development Guide](#development-guide)
11. [Testing Strategy](#testing-strategy)
12. [Troubleshooting](#troubleshooting)
13. [Performance Optimization](#performance-optimization)
14. [Maintenance & Operations](#maintenance--operations)

---

## 1. System Overview

### 1.1 Introduction

The UNZA Counseling Management System is an enterprise-grade backend application designed to manage comprehensive counseling services at the University of Zambia. This system provides a robust platform for managing appointments, client assessments, counselor workflows, and mental health academic analysis.

### 1.2 System Goals

- **Comprehensive Service Management**: Handle all aspects of university counseling services
- **Scalability**: Support growing student population and service demands
- **Security**: Protect sensitive mental health and academic data
- **Integration**: Seamlessly integrate with existing university systems
- **Analytics**: Provide insights into mental health trends and service effectiveness

### 1.3 Key Features

- **User Management**: Multi-role authentication and authorization system
- **Appointment Scheduling**: Advanced scheduling with conflict detection and reminders
- **Client Management**: Comprehensive client profiles with academic integration
- **Risk Assessment**: Automated risk assessment with escalation protocols
- **Academic Performance Integration**: Correlation analysis between mental health and academics
- **Session Management**: Complete session tracking and note-taking capabilities
- **Notification System**: Multi-channel notifications (email, SMS, in-app)
- **Reporting & Analytics**: Comprehensive dashboards and reporting tools
- **Resource Management**: Digital resource library for clients and counselors

### 1.4 System Boundaries

**In Scope:**
- Counseling appointment management
- Client and counselor management
- Risk assessment and monitoring
- Academic performance correlation
- Session documentation and tracking
- Resource management
- Reporting and analytics
- Notification services

**Out of Scope:**
- Direct integration with external healthcare systems
- Teletherapy video conferencing (planned for future)
- Mobile application development
- Advanced AI-driven mental health analysis

---

## 2. Architecture & Design

### 2.1 System Architecture

The system follows a layered architecture pattern with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│                    (REST Controllers)                       │
├─────────────────────────────────────────────────────────────┤
│                      Service Layer                          │
│                   (Business Logic)                          │
├─────────────────────────────────────────────────────────────┤
│                    Data Access Layer                        │
│                   (Repositories)                            │
├─────────────────────────────────────────────────────────────┤
│                      Database Layer                         │
│                   (PostgreSQL)                              │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Design Patterns

- **Repository Pattern**: Abstracts data access operations
- **Service Layer Pattern**: Encapsulates business logic
- **DTO Pattern**: Transfers data between layers
- **Factory Pattern**: Creates objects based on authentication source
- **Strategy Pattern**: Implements different authentication strategies
- **Observer Pattern**: Handles notifications and events

### 2.3 Entity Relationships

#### Core Entities

1. **User** (Base Entity)
   - Inheritance hierarchy for different user types
   - Common fields: username, email, password, roles
   - Audit fields: createdAt, updatedAt

2. **Client** (Extends User)
   - Student-specific information
   - Academic performance tracking
   - Risk assessment data
   - Counseling history

3. **Counselor** (Extends User)
   - Professional qualifications
   - Specialization and availability
   - Workload management

4. **Appointment**
   - Scheduling and management
   - Status tracking
   - Type classification

5. **Assessment**
   - Risk assessments
   - Self-assessments
   - Academic performance correlation

### 2.4 API Design Principles

- **RESTful Design**: Follows REST conventions
- **Versioning**: API versioning for backward compatibility
- **Consistent Response Format**: Standardized response structure
- **Error Handling**: Comprehensive error responses
- **Validation**: Input validation at multiple levels
- **Documentation**: OpenAPI/Swagger integration

---

## 3. Technology Stack

### 3.1 Backend Technologies

#### Core Framework
- **Java 17**: Latest LTS version with enhanced performance and security
- **Spring Boot 3.1.5**: Enterprise application framework with auto-configuration
- **Spring Security**: Authentication and authorization framework
- **Spring Data JPA**: Object-relational mapping and data access

#### Database & Persistence
- **PostgreSQL 15**: Primary database with advanced features
- **Flyway**: Database migration and version control
- **Redis**: Caching and session storage
- **JPA/Hibernate**: ORM framework for database operations

#### Security & Authentication
- **JWT (JSON Web Tokens)**: Stateless authentication
- **BCrypt**: Password hashing
- **OAuth2**: Integration with external authentication sources
- **Role-Based Access Control (RBAC)**: Fine-grained permissions

#### Communication & Integration
- **REST APIs**: Standard HTTP-based communication
- **WebSocket**: Real-time notifications (planned)
- **Email Services**: JavaMail for email notifications
- **OpenFeign**: Service-to-service communication

#### Monitoring & Observability
- **Spring Boot Actuator**: Application monitoring endpoints
- **Micrometer**: Metrics collection
- **Prometheus**: Metrics storage and querying
- **Grafana**: Visualization and dashboards

#### Development & Testing
- **Lombok**: Code reduction and boilerplate elimination
- **MapStruct**: DTO mapping
- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework for testing
- **Testcontainers**: Integration testing with Docker

### 3.2 Infrastructure Technologies

#### Containerization
- **Docker**: Application containerization
- **Docker Compose**: Multi-container orchestration
- **Multi-stage Builds**: Optimized container images

#### Deployment & Orchestration
- **Kubernetes**: Container orchestration (planned)
- **Environment-specific configurations**: Development, staging, production

#### Caching & Performance
- **Redis**: Distributed caching
- **Spring Cache**: Cache abstraction layer
- **Connection Pooling**: Database connection optimization

---

## 4. API Documentation

### 4.1 Base URL and Authentication

```
Base URL: http://localhost:8080/api
Authentication: JWT Bearer Token
```

### 4.2 Authentication Endpoints

#### User Registration
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "student123",
  "email": "student@unza.zm",
  "password": "securepassword",
  "firstName": "John",
  "lastName": "Doe",
  "studentId": "2023001234",
  "gender": "MALE",
  "department": "Computer Science",
  "program": "BSc Computer Science",
  "yearOfStudy": 2
}
```

#### User Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "student123",
  "password": "securepassword",
  "authenticationSource": "INTERNAL"
}
```

#### Response Format
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "student123",
      "email": "student@unza.zm",
      "roles": ["ROLE_STUDENT"]
    }
  },
  "message": "Login successful"
}
```

### 4.3 User Management Endpoints

#### Get User Profile
```http
GET /api/users/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Update User Profile
```http
PUT /api/users/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Smith",
  "phoneNumber": "+260977123456",
  "bio": "Computer Science student"
}
```

### 4.4 Appointment Management Endpoints

#### Create Appointment
```http
POST /api/appointments
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "title": "Initial Counseling Session",
  "studentId": 1,
  "counselorId": 2,
  "appointmentDate": "2024-01-15T10:00:00",
  "duration": 60,
  "type": "INITIAL_CONSULTATION",
  "description": "First meeting to discuss concerns"
}
```

#### Get Appointments with Filtering
```http
GET /api/appointments?page=0&size=10&sort=appointmentDate,desc&status=SCHEDULED
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Update Appointment Status
```http
PUT /api/appointments/1/status
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "status": "CONFIRMED",
  "notes": "Student confirmed attendance"
}
```

### 4.5 Client Management Endpoints

#### Create Client
```http
POST /api/clients
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "studentId": "2023001234",
  "programme": "BSc Computer Science",
  "faculty": "Science",
  "yearOfStudy": 2,
  "gpa": 3.2,
  "emergencyContactName": "Jane Doe",
  "emergencyContactPhone": "+260977123456",
  "emergencyContactRelationship": "Mother",
  "medicalHistory": "No significant medical history",
  "counselingHistory": "No previous counseling",
  "consentToTreatment": true
}
```

#### Get Client with Risk Assessment
```http
GET /api/clients/1?include=riskAssessment,academicPerformance
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 4.6 Assessment Endpoints

#### Submit Risk Assessment
```http
POST /api/risk-assessments
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "clientId": 1,
  "assessmentType": "SUICIDE_RISK",
  "responses": {
    "suicidal_thoughts": "NEVER",
    "hopelessness": "SOMETIMES",
    "sleep_disturbance": "OFTEN",
    "appetite_changes": "NEVER",
    "energy_level": "LOW"
  },
  "riskLevel": "HIGH",
  "notes": "Client reports increased stress due to academic pressure"
}
```

#### Get Self-Assessment Results
```http
GET /api/self-assessments/results?clientId=1&startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 4.5 Response Format Standards

#### Success Response
```json
{
  "success": true,
  "data": {
    // Response data
  },
  "message": "Operation completed successfully",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

#### Error Response
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": {
      "email": "Email is required",
      "password": "Password must be at least 8 characters"
    }
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## 5. Database Schema

### 5.1 Core Tables

#### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(120) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    student_id VARCHAR(50) UNIQUE,
    phone_number VARCHAR(20),
    profile_picture VARCHAR(255),
    bio TEXT,
    gender VARCHAR(20) NOT NULL,
    date_of_birth TIMESTAMP,
    department VARCHAR(100),
    program VARCHAR(100),
    year_of_study INTEGER,
    is_active BOOLEAN DEFAULT true,
    email_verified BOOLEAN DEFAULT false,
    last_login_at TIMESTAMP,
    reset_password_token VARCHAR(255),
    reset_password_expiry TIMESTAMP,
    authentication_source VARCHAR(20) DEFAULT 'INTERNAL',
    license_number VARCHAR(100),
    specialization VARCHAR(200),
    qualifications TEXT,
    years_of_experience INTEGER,
    available_for_appointments BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Clients Table
```sql
CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY REFERENCES users(id),
    student_id VARCHAR(50) UNIQUE,
    programme VARCHAR(100),
    faculty VARCHAR(100),
    year_of_study INTEGER,
    gpa DECIMAL(3,2),
    client_status VARCHAR(20) DEFAULT 'ACTIVE',
    risk_level VARCHAR(20) DEFAULT 'LOW',
    risk_score INTEGER DEFAULT 0,
    total_sessions INTEGER DEFAULT 0,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_session_date TIMESTAMP,
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    emergency_contact_relationship VARCHAR(50),
    medical_history TEXT,
    counseling_history TEXT,
    referral_source VARCHAR(100),
    consent_to_treatment BOOLEAN DEFAULT false,
    notes TEXT
);
```

#### Appointments Table
```sql
CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    student_id BIGINT REFERENCES users(id),
    counselor_id BIGINT REFERENCES users(id),
    appointment_date TIMESTAMP NOT NULL,
    duration INTEGER DEFAULT 60,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    description TEXT,
    meeting_link VARCHAR(500),
    location VARCHAR(200),
    cancellation_reason TEXT,
    reminder_sent BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 5.2 Assessment Tables

#### Risk Assessments Table
```sql
CREATE TABLE risk_assessments (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES clients(id),
    assessment_type VARCHAR(50) NOT NULL,
    responses JSONB NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    risk_score INTEGER NOT NULL,
    assessor_id BIGINT REFERENCES users(id),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Self Assessments Table
```sql
CREATE TABLE self_assessments (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES clients(id),
    assessment_type VARCHAR(50) NOT NULL,
    responses JSONB NOT NULL,
    score INTEGER NOT NULL,
    interpretation VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Academic Performance Table
```sql
CREATE TABLE academic_performance (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES clients(id),
    semester VARCHAR(20) NOT NULL,
    gpa DECIMAL(3,2) NOT NULL,
    credits_earned INTEGER NOT NULL,
    credits_attempted INTEGER NOT NULL,
    academic_standing VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 5.3 Relationships and Constraints

#### Foreign Key Constraints
- All foreign keys enforce referential integrity
- Cascade deletes where appropriate
- Unique constraints on critical fields

#### Indexes
- Primary keys: Automatic indexing
- Foreign keys: Performance optimization
- Search fields: Username, email, student_id
- Date fields: Appointment dates, created_at

#### Data Validation
- NOT NULL constraints on required fields
- CHECK constraints for enum values
- UNIQUE constraints for business rules

---

## 6. Security Implementation

### 6.1 Authentication Architecture

#### JWT-Based Authentication
- Stateless authentication using JSON Web Tokens
- Token expiration and refresh mechanism
- Secure token storage and transmission

#### Multi-Source Authentication
- Internal authentication (database)
- External authentication (OAuth2, LDAP)
- Flexible authentication source switching

### 6.2 Authorization Framework

#### Role-Based Access Control (RBAC)
```java
public enum ERole {
    SUPER_ADMIN,  // Full system access
    ADMIN,        // Administrative functions
    COUNSELOR,    // Counseling services
    STUDENT,      // Student access
    CLIENT        // Client access
}
```

#### Permission-Based Authorization
- Fine-grained permissions for specific operations
- Method-level security annotations
- Dynamic permission checking

### 6.3 Security Configuration

#### CORS Configuration
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
    configuration.setAllowCredentials(false);
    return source;
}
```

#### Security Filters
- JWT authentication filter
- Rate limiting protection
- Input validation and sanitization

### 6.4 Data Protection

#### Encryption
- Password hashing with BCrypt
- Sensitive data encryption at rest
- SSL/TLS for data in transit

#### Audit Logging
- Comprehensive audit trail
- Security event logging
- Compliance reporting

### 6.5 Security Best Practices

#### Input Validation
- Comprehensive validation at all layers
- SQL injection prevention
- XSS protection

#### Error Handling
- Secure error responses
- No sensitive information leakage
- Proper exception handling

---

## 7. Configuration Management

### 7.1 Environment Configuration

#### Profile-Based Configuration
- Development: Detailed logging, relaxed security
- Staging: Production-like with additional monitoring
- Production: Optimized performance and security

#### Configuration Files
```yaml
# application.yml - Base configuration
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:development}
  
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/unza_counseling}
    username: ${DATABASE_USERNAME:postgres}
    password: ${DATABASE_PASSWORD:password}
    
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    
jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: ${JWT_EXPIRATION:86400000}
```

### 7.2 External Configuration

#### Environment Variables
- Database connection strings
- API keys and secrets
- Service endpoints
- Feature flags

#### Configuration Management
- Centralized configuration
- Environment-specific overrides
- Secure credential management

### 7.3 Application Properties

#### Database Configuration
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Production: validate, Development: create-drop
    show-sql: false       # Production: false, Development: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
```

#### Security Configuration
```yaml
security:
  jwt:
    secret: ${JWT_SECRET}
    expiration: 86400000    # 24 hours
    refresh-expiration: 604800000  # 7 days
```

#### Monitoring Configuration
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized
      roles: ADMIN
```

---

## 8. Monitoring & Observability

### 8.1 Health Checks

#### Custom Health Indicators
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        try {
            // Database connectivity check
            return Health.up()
                .withDetail("database", "PostgreSQL")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

#### System Health Monitoring
- Database connectivity
- Redis cache status
- External service availability
- Resource utilization

### 8.2 Metrics Collection

#### Business Metrics
```java
@Component
public class BusinessMetricsHelper {
    
    @Bean
    public Counter appointmentsCreatedCounter() {
        return Counter.builder("appointments.created")
            .description("Number of appointments created")
            .register(meterRegistry);
    }
    
    @Bean
    public Gauge activeUsersGauge() {
        return Gauge.builder("users.active")
            .description("Number of active users")
            .register(meterRegistry);
    }
}
```

#### Performance Metrics
- API response times
- Database query performance
- Cache hit/miss ratios
- Error rates

### 8.3 Logging Strategy

#### Structured Logging
```java
@Slf4j
@RestController
public class AppointmentController {
    
    @PostMapping("/appointments")
    public ResponseEntity<?> createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
        log.info("Creating appointment for student: {}, counselor: {}", 
                request.getStudentId(), request.getCounselorId());
        
        try {
            // Business logic
            log.info("Appointment created successfully with ID: {}", appointment.getId());
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            log.error("Failed to create appointment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
```

#### Log Levels
- **ERROR**: System errors and exceptions
- **WARN**: Potential issues and warnings
- **INFO**: General application flow
- **DEBUG**: Detailed debugging information

### 8.4 Monitoring Stack

#### Prometheus Integration
```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: unza-counseling
      environment: ${SPRING_PROFILES_ACTIVE}
```

#### Grafana Dashboards
- Application performance metrics
- Business KPIs and trends
- System resource utilization
- Error tracking and analysis

---

## 9. Deployment Guide

### 9.1 Prerequisites

#### System Requirements
- **Java 17** or higher
- **Maven 3.8+**
- **PostgreSQL 15+**
- **Redis 7+**
- **Docker** (optional)
- **Docker Compose** (optional)

#### Environment Setup
```bash
# Install Java 17
sudo apt update
sudo apt install openjdk-17-jdk

# Install Maven
sudo apt install maven

# Install PostgreSQL
sudo apt install postgresql postgresql-contrib

# Install Redis
sudo apt install redis-server
```

### 9.2 Database Setup

#### PostgreSQL Configuration
```sql
-- Create database
CREATE DATABASE unza_counseling_dev;

-- Create user
CREATE USER unza_user WITH PASSWORD 'secure_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE unza_counseling_dev TO unza_user;

-- Connect and run migrations
\c unza_counseling_dev
```

#### Flyway Migrations
```bash
# Run migrations
mvn flyway:migrate -Dspring-boot.run.profiles=development

# Check migration status
mvn flyway:info
```

### 9.3 Application Configuration

#### Environment Variables
```bash
export DATABASE_URL="jdbc:postgresql://localhost:5432/unza_counseling_dev"
export DATABASE_USERNAME="unza_user"
export DATABASE_PASSWORD="secure_password"
export JWT_SECRET="your-super-secure-jwt-secret-key"
export REDIS_HOST="localhost"
export REDIS_PORT="6379"
```

#### Application Properties
```yaml
# application-development.yml
spring:
  profiles: development
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}
```

### 9.4 Docker Deployment

#### Dockerfile
```dockerfile
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY target/unza-counseling-backend-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Docker Compose
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    depends_on:
      - postgres
      - redis
  
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: unza_counseling
      POSTGRES_USER: unza_user
      POSTGRES_PASSWORD: secure_password
    ports:
      - "5432:5432"
  
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
```

### 9.5 Production Deployment

#### Build and Package
```bash
# Clean build
mvn clean package -DskipTests

# Create production JAR
java -jar target/unza-counseling-backend-*.jar \
  --spring.profiles.active=production
```

#### Systemd Service
```ini
[Unit]
Description=UNZA Counseling Backend
After=syslog.target

[Service]
User=unza-app
ExecStart=/usr/bin/java -jar /opt/unza-counseling/app.jar
Restart=always
Environment=SPRING_PROFILES_ACTIVE=production

[Install]
WantedBy=multi-user.target
```

#### Nginx Configuration
```nginx
server {
    listen 80;
    server_name counseling.unza.zm;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## 10. Development Guide

### 10.1 Project Structure

```
src/main/java/zm/unza/counseling/
├── config/              # Configuration classes
├── controller/          # REST controllers
├── dto/                 # Data Transfer Objects
├── entity/              # JPA entities
├── exception/           # Custom exceptions
├── repository/          # Data access layer
├── security/            # Security components
├── service/             # Business logic
└── util/                # Utility classes

src/test/java/zm/unza/counseling/
├── unit/               # Unit tests
├── integration/        # Integration tests
└── performance/        # Performance tests
```

### 10.2 Coding Standards

#### Java Standards
- Follow Java naming conventions
- Use Lombok for boilerplate reduction
- Implement proper exception handling
- Use dependency injection

#### Code Organization
```java
// Package structure
package zm.unza.counseling.controller;

// Import organization
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.*;
import zm.unza.counseling.dto.response.*;
import zm.unza.counseling.service.*;

// Class structure
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    
    private final AppointmentService appointmentService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> getAppointments() {
        // Implementation
    }
}
```

### 10.3 Testing Strategy

#### Unit Testing
```java
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    
    @Mock
    private AppointmentRepository appointmentRepository;
    
    @InjectMocks
    private AppointmentService appointmentService;
    
    @Test
    void createAppointment_shouldReturnCreatedAppointment() {
        // Test implementation
    }
}
```

#### Integration Testing
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AppointmentControllerIT {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void createAppointment_shouldReturnCreatedAppointment() {
        // Integration test implementation
    }
}
```

### 10.4 Development Workflow

#### Git Workflow
- Feature branches for new functionality
- Pull requests for code review
- Main branch for stable releases
- Semantic versioning for releases

#### Development Commands
```bash
# Run application
mvn spring-boot:run

# Run tests
mvn test

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=development

# Package application
mvn clean package

# Run specific test
mvn test -Dtest=AppointmentServiceTest
```

---

## 11. Testing Strategy

### 11.1 Test Types

#### Unit Tests
- Test individual components in isolation
- Mock dependencies
- Fast execution
- High coverage

#### Integration Tests
- Test component interactions
- Use Testcontainers for real databases
- Test API endpoints
- Database integration

#### Performance Tests
- Load testing with Gatling
- Stress testing scenarios
- Performance benchmarking
- Bottleneck identification

### 11.2 Test Configuration

#### Test Dependencies
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

#### Test Configuration
```yaml
# application-test.yml
spring:
  profiles: test
  datasource:
    url: jdbc:tc:postgresql:15:///testdb
  jpa:
    hibernate:
      ddl-auto: create-drop
```

### 11.3 Test Examples

#### Service Layer Test
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void registerUser_shouldReturnUserWithEncodedPassword() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password");
        
        // When
        User result = userService.register(request);
        
        // Then
        assertNotNull(result);
        verify(passwordEncoder).encode("password");
    }
}
```

#### Controller Test
```java
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AuthService authService;
    
    @Test
    void login_shouldReturnToken() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("user");
        request.setPassword("password");
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectToJson(request)))
                .andExpect(status().isOk());
    }
}
```

---

## 12. Troubleshooting

### 12.1 Common Issues

#### Database Connection Issues
```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Check connection
psql -h localhost -U unza_user -d unza_counseling_dev

# Check application logs
tail -f logs/unza-counseling-dev.log
```

#### Redis Connection Issues
```bash
# Check Redis status
sudo systemctl status redis

# Test connection
redis-cli ping

# Check configuration
redis-cli config get *
```

#### JWT Authentication Issues
```bash
# Check JWT secret configuration
echo $JWT_SECRET

# Verify token format
# Tokens should be in format: header.payload.signature

# Check expiration
# Default expiration: 24 hours
```

### 12.2 Log Analysis

#### Application Logs
```bash
# View recent logs
tail -100f logs/unza-counseling-dev.log

# Search for errors
grep -i error logs/unza-counseling-dev.log

# Search for specific user
grep "user123" logs/unza-counseling-dev.log
```

#### System Logs
```bash
# Check system logs
sudo journalctl -u unza-counseling

# Check Docker logs
docker logs unza-counseling-app

# Check PostgreSQL logs
sudo tail -f /var/log/postgresql/postgresql-15-main.log
```

### 12.3 Performance Issues

#### Slow Database Queries
```sql
-- Check slow queries
SELECT query, mean_time, calls
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;

-- Check table statistics
ANALYZE;

-- Check index usage
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read, idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;
```

#### Memory Issues
```bash
# Check Java memory usage
jstat -gc <pid>

# Check system memory
free -h

# Check application memory
jmap -heap <pid>
```

### 12.4 Debugging Tools

#### Spring Boot Actuator
```bash
# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Environment
curl http://localhost:8080/actuator/env
```

#### Database Tools
```bash
# PostgreSQL command line
psql -h localhost -U unza_user -d unza_counseling_dev

# Redis command line
redis-cli

# Database monitoring
pg_top
```

---

## 13. Performance Optimization

### 13.1 Database Optimization

#### Indexing Strategy
```sql
-- Primary keys (automatic)
-- Foreign keys (performance)
CREATE INDEX idx_appointments_student_id ON appointments(student_id);
CREATE INDEX idx_appointments_counselor_id ON appointments(counselor_id);
CREATE INDEX idx_appointments_date ON appointments(appointment_date);

-- Search indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_clients_student_id ON clients(student_id);
```

#### Query Optimization
```java
// Use pagination
Page<Appointment> appointments = appointmentRepository.findAll(pageable);

// Use projections for specific fields
@Query("SELECT a.id, a.title, a.appointmentDate FROM Appointment a WHERE a.status = :status")
List<AppointmentSummary> findAppointmentsByStatus(String status);

// Use batch operations
@Modifying
@Query("UPDATE Appointment a SET a.status = :status WHERE a.id IN :ids")
void updateStatuses(List<Long> ids, String status);
```

### 13.2 Caching Strategy

#### Redis Configuration
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1 hour
      use-key-prefix: true
```

#### Cache Annotations
```java
@Service
public class UserService {
    
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    @CacheEvict(value = "users", key = "#user.id")
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
```

### 13.3 Application Performance

#### Connection Pooling
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

#### JVM Optimization
```bash
# JVM options
-XX:+UseG1GC
-Xms1g
-Xmx2g
-XX:MaxMetaspaceSize=512m
-XX:+HeapDumpOnOutOfMemoryError
```

### 13.4 Monitoring Performance

#### Metrics Collection
```java
@Timed(value = "service.operation", description = "Time taken for operation")
public void performOperation() {
    // Business logic
}

@Counted(value = "service.calls", description = "Number of service calls")
public void serviceMethod() {
    // Service logic
}
```

#### Performance Testing
```java
@LoadTest
public class PerformanceTest {
    
    @Test
    void testHighLoad() {
        // Performance test implementation
    }
}
```

---

## 14. Maintenance & Operations

### 14.1 Backup and Recovery

#### Database Backup
```bash
# Full database backup
pg_dump -h localhost -U unza_user unza_counseling_dev > backup_$(date +%Y%m%d).sql

# Automated backup script
#!/bin/bash
BACKUP_DIR="/backups"
DATE=$(date +%Y%m%d_%H%M%S)
pg_dump -h localhost -U unza_user unza_counseling_dev > $BACKUP_DIR/backup_$DATE.sql
gzip $BACKUP_DIR/backup_$DATE.sql
```

#### Redis Backup
```bash
# Redis RDB backup
redis-cli BGSAVE

# Automated backup
redis-cli BGSAVE && sleep 5 && cp /var/lib/redis/dump.rdb /backups/redis_backup_$(date +%Y%m%d).rdb
```

### 14.2 Monitoring and Alerting

#### Health Check Monitoring
```bash
# Health check script
#!/bin/bash
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)
if [ $RESPONSE -ne 200 ]; then
    echo "Application health check failed"
    # Send alert
fi
```

#### Performance Monitoring
```bash
# Memory usage monitoring
#!/bin/bash
MEMORY_USAGE=$(free | grep Mem | awk '{printf "%.1f", $3/$2 * 100.0}')
if [ $(echo "$MEMORY_USAGE > 80" | bc) -eq 1 ]; then
    echo "High memory usage: $MEMORY_USAGE%"
    # Send alert
fi
```

### 14.3 Updates and Patches

#### Application Updates
```bash
# Stop application
sudo systemctl stop unza-counseling

# Backup current version
cp /opt/unza-counseling/app.jar /backups/app_$(date +%Y%m%d).jar

# Deploy new version
cp new-app.jar /opt/unza-counseling/app.jar

# Start application
sudo systemctl start unza-counseling

# Verify deployment
curl http://localhost:8080/actuator/health
```

#### Database Migrations
```bash
# Run migrations
mvn flyway:migrate -Dspring-boot.run.profiles=production

# Check migration status
mvn flyway:info

# Rollback if needed
mvn flyway:undo
```

### 14.4 Security Maintenance

#### Security Updates
- Regular dependency updates
- Security patch application
- Vulnerability scanning
- Penetration testing

#### Access Control
- Regular access review
- Password policy enforcement
- Two-factor authentication
- Audit log analysis

---

## Conclusion

This comprehensive documentation provides all the necessary information for understanding, developing, deploying, and maintaining the UNZA Counseling Management System backend. The system is designed with enterprise-grade principles, ensuring scalability, security, and maintainability.

For additional support or questions, please contact the development team at dev@unza.zm.

---

**Document Version**: 1.0.0
**Last Updated**: December 2025
**Next Review**: March 2026