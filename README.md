# Library Management System

## Project Overview

The Library Management System is a comprehensive backend application developed as the final project (Bitirme Projesi)
for the **Patika.dev & Getir Java Spring Boot Bootcamp**. This system provides a robust platform for managing library
operations including book inventory, user management, borrowing and returning processes, with automated penalty
management and real-time notifications.

### Key Features

**Book Management**

- Complete CRUD operations for books with validation
- Advanced search functionality (by title, author, genre, ISBN)
- Book availability tracking with real-time updates
- Support for multiple book quantities

**User Management**

- Role-based user system (Admin, Librarian, Patron)
- User registration and authentication with JWT security
- User status management (Active, Suspended, Pending, Deleted)
- Automatic account maintenance for inactive users

**Borrowing & Returning System**

- Secure borrowing with eligibility checks
- Automated due date calculation (14-day default)
- Real-time book availability updates using WebFlux
- Comprehensive borrowing history and tracking

**Advanced Features**

- **Penalty System**: Automatic suspension for users with 3+ late returns
- **PDF Export**: Generate detailed reports for borrowings, overdue items, and user history
- **Real-time Updates**: WebFlux-powered book availability streaming
- **Monitoring**: Spring Boot Actuator with custom health indicators
- **Comprehensive Testing**: Unit, integration, and container tests

## Technology Stack

### Core Technologies

- **Java 21** - Latest LTS version with modern language features
- **Spring Boot 3.4.5** - Latest stable framework version
- **PostgreSQL** - Production database
- **Maven** - Dependency management and build tool

### Spring Framework Components

- **Spring Security** - JWT-based authentication and authorization
- **Spring Data JPA** - Data persistence with Hibernate
- **Spring WebFlux** - Reactive programming for real-time features
- **Spring Boot Actuator** - Application monitoring and health checks
- **Spring Scheduling** - Automated penalty and maintenance tasks

### Additional Libraries & Tools

- **JWT (0.11.5)** - JSON Web Token implementation
- **MapStruct (1.5.5)** - Type-safe bean mapping
- **OpenAPI 3.0/Swagger** - API documentation
- **iText PDF** - PDF generation for reports
- **TestContainers** - Integration testing with real PostgreSQL
- **H2** - In-memory database for unit tests
- **Docker** - Containerization support

## Prerequisites

Before running the application, ensure you have the following installed:

- **Java 21** or higher
- **Maven 3.8+**
- **PostgreSQL 13+** (for local development)
- **Docker & Docker Compose** (optional, for containerized deployment)
- **Git** (for cloning the repository)

## Running the Application Locally

### 1. Clone the Repository

```bash
git clone <repository-url>
cd library-management-backend
```

### 2. Database Setup

#### Option A: Local PostgreSQL Installation

1. Install PostgreSQL and create a database:

```sql
CREATE
DATABASE library_db;
CREATE
USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE
library_db TO postgres;
```

2. Update `application-dev.yml` if needed:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/library_db
    username: postgres
    password: postgres
```

#### Option B: Using Docker for PostgreSQL

```bash
docker run --name postgres-db \
  -e POSTGRES_DB=library_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15
```

### 3. Configure Application

Set the active profile to `dev` in `application.yml`:

```yaml
spring:
  profiles:
    active: dev
```

### 4. Build and Run

```bash
# Install dependencies and compile
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Docker Deployment

### Using Docker Compose (Recommended)

1. Create a `docker-compose.yml` file:

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    container_name: library-postgres
    environment:
      POSTGRES_DB: library_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  app:
    build: .
    container_name: library-app
    depends_on:
      - postgres
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: docker
    volumes:
      - ./logs:/app/logs

volumes:
  postgres_data:
```

2. Create a `Dockerfile`:

```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/library-management-backend-*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
```

3. Build and run:

```bash
# Build the application
mvn clean package

# Start services
docker-compose up -d

# View logs
docker-compose logs -f app
```

### Standalone Docker

```bash
# Build the image
docker build -t library-management-system .

# Run with external PostgreSQL
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/library_db \
  library-management-system
```

## API Documentation

### Swagger UI

Once the application is running, access the interactive API documentation at:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Main API Endpoints

#### Authentication

- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/register` - User registration

#### Book Management

- `GET /api/v1/books` - List all books (paginated)
- `GET /api/v1/books/{id}` - Get book by ID
- `GET /api/v1/books/isbn/{isbn}` - Get book by ISBN
- `POST /api/v1/books` - Create new book (Librarian/Admin)
- `PUT /api/v1/books/{id}` - Update book (Librarian/Admin)
- `DELETE /api/v1/books/{id}` - Delete book (Librarian/Admin)
- `GET /api/v1/books/search/**` - Search books by various criteria

#### User Management

- `GET /api/v1/users` - List users (Admin/Librarian)
- `GET /api/v1/users/{id}` - Get user details
- `POST /api/v1/users` - Create user (Admin)
- `POST /api/v1/users/librarians` - Create librarian (Admin/Librarian)
- `PUT /api/v1/users/{id}` - Update user
- `DELETE /api/v1/users/{id}` - Soft delete user (Admin)

#### Borrowing System

- `POST /api/v1/borrowings` - Borrow a book
- `PUT /api/v1/borrowings/{id}/return` - Return a book
- `GET /api/v1/borrowings` - List all borrowings (Admin/Librarian)
- `GET /api/v1/borrowings/my-borrowings` - Get current user's borrowings
- `GET /api/v1/borrowings/overdue` - Get overdue borrowings (Admin/Librarian)

#### PDF Export

- `GET /api/v1/export/borrowings/all` - Export all borrowings (Admin)
- `GET /api/v1/export/borrowings/user/{userId}` - Export user borrowings
- `GET /api/v1/export/borrowings/overdue` - Export overdue borrowings
- `GET /api/v1/export/borrowings/book/{bookId}` - Export book history
- `GET /api/v1/export/borrowings/date-range` - Export by date range

#### Real-time Features

- `GET /api/v1/books/availability/stream` - Book availability event stream (SSE)

### Authentication

All protected endpoints require a Bearer token in the Authorization header:

```
Authorization: Bearer <JWT_TOKEN>
```

## Database Schema

### Core Entities

```sql
-- Users table
CREATE TABLE users
(
    id                  BIGSERIAL PRIMARY KEY,
    email               VARCHAR(255) UNIQUE NOT NULL,
    password            VARCHAR(255)        NOT NULL,
    first_name          VARCHAR(255)        NOT NULL,
    last_name           VARCHAR(255)        NOT NULL,
    phone_number        VARCHAR(11) UNIQUE  NOT NULL,
    role                VARCHAR(50)         NOT NULL,
    status              VARCHAR(50)         NOT NULL,
    max_allowed_borrows INTEGER DEFAULT 3,
    suspension_end_date DATE,
    deleted             BOOLEAN DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    deleted_by          VARCHAR(255),
    created_at          TIMESTAMP           NOT NULL,
    created_by          VARCHAR(255)        NOT NULL,
    updated_at          TIMESTAMP           NOT NULL,
    updated_by          VARCHAR(255)        NOT NULL,
    version             INTEGER
);

-- Books table
CREATE TABLE books
(
    id               BIGSERIAL PRIMARY KEY,
    isbn             VARCHAR(13) UNIQUE NOT NULL,
    title            VARCHAR(255)       NOT NULL,
    author           VARCHAR(255)       NOT NULL,
    publication_year INTEGER            NOT NULL,
    publisher        VARCHAR(255)       NOT NULL,
    genre            VARCHAR(50)        NOT NULL,
    image_url        VARCHAR(500),
    description      TEXT,
    available        BOOLEAN DEFAULT TRUE,
    quantity         INTEGER DEFAULT 1,
    created_at       TIMESTAMP          NOT NULL,
    created_by       VARCHAR(255)       NOT NULL,
    updated_at       TIMESTAMP          NOT NULL,
    updated_by       VARCHAR(255)       NOT NULL,
    version          INTEGER
);

-- Borrowings table
CREATE TABLE borrowing
(
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT       NOT NULL REFERENCES users (id),
    book_id       BIGINT       NOT NULL REFERENCES books (id),
    borrow_date   DATE         NOT NULL,
    due_date      DATE         NOT NULL,
    return_date   DATE,
    status        VARCHAR(50)  NOT NULL,
    returned_late BOOLEAN DEFAULT FALSE,
    created_at    TIMESTAMP    NOT NULL,
    created_by    VARCHAR(255) NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,
    updated_by    VARCHAR(255) NOT NULL,
    version       INTEGER
);
```

### Entity Relationships

- **User** (1) ↔ (N) **Borrowing**: A user can have multiple borrowings
- **Book** (1) ↔ (N) **Borrowing**: A book can be borrowed multiple times
- **BaseEntity**: Provides auditing fields (created_at, created_by, updated_at, updated_by, version)

### Enumerations

```java
// User roles
enum UserRole {PATRON, LIBRARIAN, ADMIN}

// User status
enum UserStatus {PENDING, ACTIVE, SUSPENDED, DELETED}

// Borrowing status  
enum BorrowingStatus {ACTIVE, RETURNED, OVERDUE}

// Book genres
enum Genre {
    FICTION, SCIENCE_FICTION, FANTASY, MYSTERY,
    THRILLER, HORROR, ROMANCE, HISTORICAL_FICTION,
    BIOGRAPHY, AUTOBIOGRAPHY, MEMOIR, POETRY,
    DRAMA, COMEDY, CHILDREN, YOUNG_ADULT,
    NON_FICTION, SCIENCE, HISTORY, PHILOSOPHY,
    RELIGION, PSYCHOLOGY, SELF_HELP, BUSINESS,
    ECONOMICS, POLITICS, TRAVEL, COOKING,
    ART, MUSIC, REFERENCE, TEXTBOOK, OTHER
}
```

## Testing

The project includes comprehensive testing with different strategies:

### Running Tests

```bash
# Run all tests
mvn test

```

### Test Categories

1. **Unit Tests**: Fast, isolated tests for business logic
2. **Integration Tests**: Component integration testing with TestContainers
3. **Repository Tests**: JPA repository testing with real PostgreSQL
4. **Controller Tests**: Web layer testing with MockMvc
5. **Service Tests**: Business service testing with mocked dependencies

## Monitoring and Management

### Actuator Endpoints

The application includes Spring Boot Actuator for monitoring:

- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/actuator/env` - Environment properties (Admin only)

### Custom Health Indicators

- **Database Health**: Checks PostgreSQL connectivity and table accessibility
- **Business Health**: Validates system has books and admin users
- **Library Health**: Monitors core business functionality

## Default Users

The application automatically creates default users for testing:

| Email                 | Password     | Role      | Status    |
|-----------------------|--------------|-----------|-----------|
| admin@library.com     | admin123     | ADMIN     | ACTIVE    |
| librarian@library.com | librarian123 | LIBRARIAN | ACTIVE    |
| patron@library.com    | patron123    | PATRON    | ACTIVE    |
| patron2@library.com   | patron123    | PATRON    | ACTIVE    |
| patron3@library.com   | patron123    | PATRON    | ACTIVE    |
| pending@library.com   | patron123    | PATRON    | PENDING   |
| suspended@library.com | patron123    | PATRON    | SUSPENDED |
| deleted@library.com   | patron123    | PATRON    | DELETED   |

## Development Features

### Profiles

- **dev**: Development with local PostgreSQL
- **test**: Testing with H2 in-memory database
- **docker**: Docker deployment configuration
- **prod**: Production settings

### Code Quality

- **Clean Architecture**: Layered architecture with clear separation
- **SOLID Principles**: Following best practices for maintainable code
- **Comprehensive Logging**: Detailed logging for debugging and monitoring
- **Exception Handling**: Centralized error handling with meaningful messages

## Troubleshooting

### Common Issues

**Database Connection Failed**

```
Check PostgreSQL is running and credentials are correct
Verify database exists and user has permissions
```

**Port Already in Use**

```
Change server port in application.yml:
server:
  port: 8081
```

**JWT Token Issues**

```
Ensure JWT secret is properly configured
Check token expiration time
Verify proper Bearer token format
```

### Logging

Application logs are available in the console and can be configured in `application.yml`:

```yaml
logging:
  level:
    tr.com.eaaslan.library: DEBUG
    org.springframework.security: DEBUG
```

## Future Enhancements

- **Frontend Integration**: React/Angular SPA
- **Notification System**: Email/SMS notifications
- **Advanced Reporting**: Analytics dashboard
- **Book Recommendations**: ML-based suggestions
- **Multi-library Support**: Support for multiple library branches
- **Fine Management**: Automated fine calculation and payment

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
4. Submit a pull request

## License

This project is developed for educational purposes as part of the Patika.dev & Getir Java Spring Boot Bootcamp.

---

**Developer**: eaaslan  
**Project Type**: Bootcamp Final Project (Bitirme Projesi)  
**Institution**: Patika.dev & Getir Java Spring Boot Bootcamp