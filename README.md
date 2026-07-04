# EduTaskBoard 📚

A full-featured school management system built with **Spring Boot 3.4**, **Spring Security**, **Thymeleaf**, **WebSocket (STOMP/SockJS)**, and **H2 in-memory database**.

---

## Features

| Role | Capabilities |
|------|-------------|
| **Admin** | Dashboard stats, user management, class management, registration approvals, announcements, task overview, live chat |
| **Teacher** | Task creation/editing, submission grading with feedback, student roster view, announcements, live chat |
| **Student** | View & submit assignments, view grades + feedback, announcements, profile, live chat |

### Highlights
- **Registration with Admin Approval** — new users register publicly; admin must approve before they can log in
- **Real-time Messaging** — WebSocket/STOMP chat with multiple rooms (General, Teachers, Students, Class rooms)
- **Animated UI** — slide-in cards, hover effects, floating decorations, stat counter animations
- **Role-based routing** — each role gets a dedicated portal after login

---

## Quick Start

### Requirements
- Java 21+
- Maven 3.8+

### Run
```bash
cd edutaskboard
mvn spring-boot:run
```

Open **http://localhost:8080**

---

## Demo Credentials

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Teacher | `teacher1` | `teacher123` |
| Teacher | `teacher2` | `teacher123` |
| Student | `student1` | `student123` |
| Student | `student2` | `student123` |

---

## Project Structure

```
src/main/java/com/edutaskboard/
├── EduTaskBoardApplication.java
├── config/           # Security, WebSocket, DataInitializer, UserDetails
├── controller/       # Auth, Admin, Teacher, Student controllers
├── dto/              # ChatPayload, ChatResponse
├── model/            # User, SchoolClass, Task, TaskSubmission, Announcement, ChatMessage
├── repository/       # JPA repositories
├── service/          # Business logic services
└── websocket/        # STOMP message handler

src/main/resources/
├── templates/
│   ├── auth/         # login, register
│   ├── admin/        # dashboard, users, classes, tasks, announcements
│   ├── teacher/      # dashboard, tasks, grade-form, submissions, students, announcements
│   ├── student/      # dashboard, tasks, task-detail, grades, announcements, profile
│   └── shared/       # chat, layout-fragments
├── static/
│   ├── css/main.css
│   └── js/app.js
└── application.properties
```

---

## H2 Console

http://localhost:8080/h2-console  
JDBC URL: `jdbc:h2:mem:edutaskboard` | User: `sa` | Password: *(blank)*

---

## Switch to PostgreSQL (Production)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/edutaskboard
spring.datasource.username=your_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

Add to `pom.xml`:
```xml
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>runtime</scope>
</dependency>
```
