Patika.dev & Getir Java Spring Boot Bootcamp
Bitirme Projesi
Library Management System Project Overview
This project aims to develop a comprehensive Library Management System using Spring Boot 3 (or
the latest stable version), Java 21, and a PostgreSQL database. The system will allow librarians to
manage books, users, and borrowing/returning processes. It will also include user authentication,
authorization, API documentation, and testing.
Functional Requirements
1. Book Management
   ● Add a Book:
   ○ Librarians should be able to add new books to the library system, including details
   such as title, author, ISBN, publication date, and genre.
   ○ Data validation should be implemented.
   ● View Book Details:
   ○ Users (librarians and patrons) should be able to view detailed information about a
   book.
   ● Search for Books:
   ○ Users should be able to search for books by title, author, ISBN, or genre.
   ○ Implement pagination for search results.
   ● Update Book Information:
   ○ Librarians should be able to update book information.
   ● Delete a Book:
   ○ Librarians should be able to delete a book from the system.
2. User Management
   ● Register a User:
   ○ Users (patrons and librarians) should be able to register with the system, providing
   personal information (name, contact details, etc.).
   ○ Implement user role management (librarian or patron).
   ● View User Details:
   ○ Librarians should be able to view user details.
   ● Update User Information:
   ○ Librarians should be able to update user information.
   ● Delete a User:
   ○ Librarians should be able to delete a user.
   ● User Authentication and Authorization:
   ○ Implement user authentication using Spring Security and JWT.
   ○ Implement role-based authorization to restrict access to specific functionalities.
3. Borrowing and Returning
   ● Borrow a Book:
   ○ Patrons should be able to borrow available books.
   ○ The system should track borrowing dates and due dates.
   ○ Implement checks for book availability and user eligibility.
   ● Return a Book:
   ○ Patrons should be able to return borrowed books.
   ○ The system should update book availability and borrowing records.
   ● View Borrowing History:
   ○ Users should be able to view their borrowing history.
   ○ Librarians should be able to view all users borrowing history.
   ● Manage Overdue Books:
   ○ The system should identify and track overdue books.
   ○ Librarians should be able to generate reports for overdue books.
   API and Technical Requirements
1. Core Requirements
   ● RESTful API
   ○ Develop a RESTful API for all functionalities.
   ○ Use appropriate HTTP methods and status codes.
   ● Database:
   ○ Use PostgreSQL as the relational database.
   ○ Implement data persistence using Spring Data JPA and Hibernate.
   ● Testing:
   ○ Write unit tests and integration tests using Spring Boot Test.
   ○ Use H2 database for in-memory testing.
   ● Security:
   ○ Implement Spring Security with JWT for authentication and authorization.
   ● Documentation:
   ○ Generate API documentation using Swagger/OpenAPI.
   ● Version Control:
   ○ Use Git for version control.
   ○ Store the project in a private GitHub repository and grant access only to relevant
   individuals.
   ● Build Tool:
   ○ Use Maven or Gradle as a build tool.
   ● Clean Code:
   ○ Adhere to clean code principles.
   ● Logging:
   ○ Implement a comprehensive logging mechanism for the application using a logging
   framework (e.g., Logback, SLF4J.
   ○ Log important events, errors, and application flow for debuggability.
   ● Postman Collection:
   ○ Include a complete Postman Collection file with pre-configured requests for all API
   endpoints.
2. Optional Features (Bonus)
   ● Reactive Programming:
   ○ Implement a real-time book availability update feature using Spring WebFlux.
   ● Dockerization:
   ○ Containerize the application using Docker and Docker Compose.
   Project Submission Requirements
1.
GitHub Repository:
○ Create a private GitHub repository for the project.
○ Grant access to instructor(s) and evaluator(s) only.
○ Include a thorough history showing progressive development.
2. Documentation:
   ○ README file containing:
   ■ Project overview and features
   ■ Technology stack used
   ■ Detailed instructions for running the application locally
   ■ If Docker is implemented, include Docker setup and run instructions
   ■ API endpoints description or reference to Swagger documentation
   ■ Database schema/design (can be a diagram)
   ■ Any additional information relevant to understanding or running the project
3. API Collection:
   ○ Include a Postman collection file with all API endpoints configured
   ○ Organize the collection logically by feature/functionality
   ○ Include example requests and expected responses
4. Code Quality and Structure:
   ○ Follow best practices for package structure
   ○ Include appropriate comments and documentation
   ○ Implement proper error handling
   ○ Follow clean code principles
5. Testing:
   ○ Include comprehensive test coverage
   ○ All tests should pass before submission 