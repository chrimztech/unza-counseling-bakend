# UNZA Counseling Management System - Enterprise Backend

![UNZA Logo](https://img.shields.io/badge/UNZA-Counseling-blue) ![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen) ![License](https://img.shields.io/badge/License-UNZA-red)

## 📋 Overview

The UNZA Counseling Management System is an enterprise-grade backend application designed to manage counseling services at the University of Zambia. This system provides comprehensive features for managing appointments, client assessments, counselor workflows, and mental health academic analysis.

## 🚀 Enterprise Features

### ✨ Core Features
- **User Management**: Role-based authentication with JWT tokens
- **Appointment Scheduling**: Advanced scheduling with conflict detection
- **Client Management**: Comprehensive client profiles and history tracking
- **Counselor Management**: Counselor schedules, specializations, and workload tracking
- **Risk Assessment**: Automated risk assessment with escalation protocols
- **Self-Assessment Tools**: Digital self-assessment questionnaires
- **Academic Performance Integration**: Correlation analysis between mental health and academic performance
- **Session Management**: Complete session tracking and note-taking
- **Notification System**: Real-time notifications via WebSocket and email
- **Reporting & Analytics**: Comprehensive reporting dashboards

### 🏗️ Enterprise Infrastructure
- **Database Migrations**: Flyway-based database version control
- **Caching Strategy**: Redis-based distributed caching
- **Monitoring & Observability**: Spring Boot Actuator with custom health indicators
- **Email Services**: HTML email templates with async processing
- **File Management**: Secure file upload and storage
- **API Documentation**: OpenAPI 3.0 with Swagger UI
- **Containerization**: Docker support with multi-stage builds
- **Environment Configuration**: Environment-specific configurations
- **Security**: Enhanced security with rate limiting and CORS
- **Testing**: Comprehensive testing framework structure

## 🛠️ Technology Stack

### Backend Technologies
- **Java 17**: Latest LTS version with enhanced performance
- **Spring Boot 3.1.5**: Enterprise application framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Data access layer with PostgreSQL
- **Spring Boot Actuator**: Monitoring and management endpoints
- **Flyway**: Database migration tool
- **Redis**: Caching and session storage
- **JWT**: Token-based authentication
- **MapStruct**: DTO mapping
- **Lombok**: Code reduction

### Database & Storage
- **PostgreSQL 15**: Primary database with advanced features
- **Redis**: Caching and temporary data storage
- **File System**: Document and file storage

### Monitoring & DevOps
- **Docker**: Containerization platform
- **Docker Compose**: Multi-container orchestration
- **Prometheus**: Metrics collection
- **Grafana**: Visualization and dashboards
- **Actuator**: Application monitoring
- **Health Checks**: Custom health indicators

### Communication
- **JavaMail**: Email service integration
- **WebSocket**: Real-time notifications (planned)
- **REST APIs**: Standard RESTful services

## 📁 Project Structure

```
unza-counseling-backend/
├── src/main/
│   ├── java/zm/unza/counseling/
│   │   ├── config/                 # Configuration classes
│   │   │   ├── cache/             # Caching configuration
│   │   │   ├── monitoring/        # Health indicators
│   │   │   ├── CorsConfig.java    # CORS configuration
│   │   │   ├── JwtConfig.java     # JWT configuration
│   │   │   ├── SecurityConfig.java # Security configuration
│   │   │   └── OpenApiConfig.java # API documentation
│   │   ├── controller/            # REST controllers
│   │   ├── dto/                   # Data Transfer Objects
│   │   │   ├── request/           # Request DTOs
│   │   │   └── response/          # Response DTOs
│   │   ├── entity/                # JPA entities
│   │   ├── exception/             # Exception handling
│   │   ├── mapper/                # DTO mappers (planned)
│   │   ├── repository/            # Data access layer
│   │   ├── security/              # Security components
│   │   ├── service/               # Business logic
│   │   │   └── impl/              # Service implementations
│   │   ├── test/                  # Test classes
│   │   ├── util/                  # Utility classes
│   │   └── CounselingManagementApplication.java
│   └── resources/
│       ├── application.yml        # Base configuration
│       ├── application-development.yml
│       ├── application-production.yml
│       └── db/migration/          # Database migrations
│           ├── V1__Create_initial_tables.sql
│           └── V2__Insert_initial_data.sql
├── Dockerfile                     # Container configuration
├── docker-compose.yml            # Multi-container setup
├── pom.xml                       # Maven dependencies
└── README.md                     # This file
```

## 🚦 Getting Started

### Prerequisites
- **Java 17** or higher
- **Maven 3.8+**
- **PostgreSQL 15+**
- **Redis 7+**
- **Docker** (optional, for containerized deployment)
- **Docker Compose** (optional)

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd unza-counseling-backend
   ```

2. **Setup PostgreSQL Database**
   ```bash
   # Create database
   createdb unza_counseling_dev
   
   # Or using Docker
   docker run --name unza-postgres-dev \
     -e POSTGRES_DB=unza_counseling_dev \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=11111111 \
     -p 5432:5432 -d postgres:15
   ```

3. **Setup Redis**
   ```bash
   # Local installation
   redis-server
   
   # Or using Docker
   docker run --name unza-redis-dev \
     -p 6379:6379 -d redis:7-alpine
   ```

4. **Configure Environment**
   ```bash
   # Copy environment template
   cp .env.example .env
   
   # Edit configuration
   nano .env
   ```

5. **Run Database Migrations**
   ```bash
   mvn flyway:migrate -Dspring-boot.run.profiles=development
   ```

6. **Build and Run**
   ```bash
   # Build the application
   mvn clean package
   
   # Run in development mode
   mvn spring-boot:run -Dspring-boot.run.profiles=development
   
   # Or run JAR directly
   java -jar target/unza-counseling-backend-0.0.1-SNAPSHOT.jar \
     --spring.profiles.active=development
   ```

### Docker Deployment

1. **Start All Services**
   ```bash
   # Development environment
   docker-compose --profile dev up -d
   
   # Production environment
   docker-compose -f docker-compose.yml up -d
   ```

2. **Access Services**
   - **Application**: http://localhost:8080
   - **API Documentation**: http://localhost:8080/swagger-ui.html
   - **Actuator**: http://localhost:8080/actuator
   - **Grafana**: http://localhost:3001 (admin/admin123)
   - **Prometheus**: http://localhost:9090
   - **pgAdmin**: http://localhost:5050 (admin@unza.zm/admin123)

## ⚙️ Configuration

### Environment Variables

#### Database Configuration
```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/unza_counseling_db
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_secure_password
```

#### Redis Configuration
```bash
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis_password
```

#### JWT Configuration
```bash
JWT_SECRET=your-super-secure-jwt-secret-key
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
```

#### Email Configuration
```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@unza.zm
MAIL_PASSWORD=your-app-password
```

#### Application Configuration
```bash
APP_ENVIRONMENT=development
LOG_LEVEL_ROOT=INFO
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

### Profiles

#### Development Profile
- Detailed logging
- SQL query logging
- Auto database initialization
- Test email mode
- Debug endpoints enabled

#### Production Profile
- Optimized logging
- Database validation only
- Email validation
- Security hardening
- Performance optimizations

## 📊 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication
All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <jwt_token>
```

### Key Endpoints

#### Authentication
- `POST /auth/login` - User login
- `POST /auth/register` - User registration
- `POST /auth/refresh` - Refresh token

#### Appointments
- `GET /appointments` - List appointments
- `POST /appointments` - Create appointment
- `PUT /appointments/{id}` - Update appointment
- `DELETE /appointments/{id}` - Cancel appointment

#### Clients
- `GET /clients` - List clients
- `POST /clients` - Create client
- `GET /clients/{id}` - Get client details
- `PUT /clients/{id}` - Update client

#### Assessments
- `POST /risk-assessments` - Create risk assessment
- `GET /risk-assessments` - List risk assessments
- `POST /self-assessments` - Submit self-assessment
- `GET /self-assessments` - List self-assessments

#### Analytics
- `GET /dashboard/stats` - Dashboard statistics
- `GET /analysis/mental-health` - Mental health analysis
- `GET /reports/academic-performance` - Academic performance reports

### OpenAPI Documentation
Visit `http://localhost:8080/swagger-ui.html` for interactive API documentation.

## 🔒 Security

### Authentication & Authorization
- JWT-based authentication
- Role-based access control (RBAC)
- Password encryption using BCrypt
- Session management

### Security Features
- CORS configuration
- Rate limiting
- SQL injection prevention
- XSS protection
- CSRF protection
- Input validation

### User Roles
- **SUPER_ADMIN**: Full system access
- **ADMIN**: Administrative functions
- **COUNSELOR**: Counseling services
- **CLIENT**: Basic client access

## 📈 Monitoring & Observability

### Health Checks
- **Database Health**: PostgreSQL connectivity
- **Redis Health**: Redis connectivity
- **Application Health**: Custom metrics
- **System Health**: Memory and CPU usage

### Metrics
- Application performance metrics
- Database query performance
- Cache hit/miss ratios
- User activity metrics
- Error rates and types

### Monitoring Stack
- **Prometheus**: Metrics collection
- **Grafana**: Visualization
- **Actuator**: Application monitoring
- **Custom Dashboards**: UNZA-specific metrics

### Alerting
- Database connection failures
- High error rates
- Performance degradation
- System resource constraints

## 📧 Email Services

### Email Templates
- Appointment confirmations
- Appointment reminders
- Risk assessment alerts
- Welcome emails
- System notifications

### Email Configuration
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

## 🔧 Caching Strategy

### Cache Layers
- **Application Cache**: Frequently accessed data
- **Database Cache**: Query result caching
- **Session Cache**: User session data
- **Static Data Cache**: Configuration and lookup data

### Redis Configuration
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000
      use-key-prefix: true
      cache-null-values: false
```

### Cache Management
- Automatic cache invalidation
- Cache warming strategies
- Performance monitoring
- Memory management

## 🚀 Deployment

### Docker Deployment
```bash
# Build production image
docker build -t unza-counseling-backend:latest .

# Run with docker-compose
docker-compose up -d
```

### Kubernetes Deployment
```yaml
# Example Kubernetes manifest
apiVersion: apps/v1
kind: Deployment
metadata:
  name: unza-counseling-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: unza-counseling-backend
  template:
    metadata:
      labels:
        app: unza-counseling-backend
    spec:
      containers:
      - name: unza-counseling-backend
        image: unza-counseling-backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
```

### Environment-Specific Deployment
```bash
# Development
mvn spring-boot:run -Dspring-boot.run.profiles=development

# Production
java -jar target/unza-counseling-backend-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=production \
  --server.port=8080
```

## 🧪 Testing

### Test Structure
```
src/test/java/zm/unza/counseling/
├── unit/                    # Unit tests
├── integration/            # Integration tests
├── performance/            # Performance tests
├── security/               # Security tests
└── Test classes/           # Specific test classes
```

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test profile
mvn test -Dspring.profiles.active=test

# Run with coverage
mvn jacoco:report

# Run performance tests
mvn test -Dtest=*Performance*
```

## 📚 Database Schema

### Key Tables
- **users**: User authentication and profiles
- **counselors**: Counselor information and specializations
- **clients**: Client profiles and academic information
- **appointments**: Appointment scheduling
- **sessions**: Counseling session records
- **risk_assessments**: Risk assessment data
- **self_assessments**: Self-assessment responses
- **academic_performance**: Academic performance tracking
- **notifications**: System notifications

### Migrations
- **V1__Create_initial_tables.sql**: Base schema
- **V2__Insert_initial_data.sql**: Default data

## 🤝 Contributing

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3. **Commit your changes** (`git commit -m 'Add amazing feature'`)
4. **Push to the branch** (`git push origin feature/amazing-feature`)
5. **Open a Pull Request**

### Coding Standards
- Follow Java coding conventions
- Add unit tests for new features
- Update documentation
- Use meaningful commit messages

## 📄 License

This project is licensed under the UNZA License - see the LICENSE file for details.

## 🆘 Support

### Documentation
- [API Documentation](http://localhost:8080/swagger-ui.html)
- [Health Checks](http://localhost:8080/actuator/health)
- [Metrics](http://localhost:8080/actuator/metrics)

### Contact
- **IT Department**: it@unza.zm
- **Development Team**: dev@unza.zm
- **Support**: support@unza.zm

### Troubleshooting
- Check application logs in `/app/logs/`
- Verify database connectivity
- Check Redis connection status
- Review health check endpoints
- Monitor resource usage

## 🔄 Changelog

### Version 1.0.0 (2025-12-20)
- ✨ Initial release with enterprise features
- 🏗️ Database migration with Flyway
- 📧 Email service implementation
- 🔍 Monitoring and health checks
- 💾 Caching with Redis
- 🐳 Docker containerization
- 📚 Comprehensive documentation

---

**UNZA Counseling Management System** - Empowering mental health services at the University of Zambia 🏫💙
