# Complaint Hub

A web-based complaint management system built to make the process of raising, tracking, assigning, and resolving complaints easier for both users and administrators.

Complaint Hub provides separate workflows for customers, administrators, and staff members, with authentication, complaint assignment, status updates, attachments, profile management, and administrative controls.

---

## What is Complaint Hub?

In many organizations, complaints are still handled through emails, phone calls, spreadsheets, or informal communication. That makes it difficult to keep track of who reported an issue, who is handling it, what has been done, and whether the problem has actually been resolved.

Complaint Hub brings these activities into one place.

A user can raise a complaint and track its progress, while administrators and staff can manage complaints, assign them to the appropriate personnel, update their status, and keep a record of the actions taken.

The project was developed as a team project, with different parts of the application being worked on and integrated into a single system.

---

## Main Features

### User / Customer

- User registration and login
- Raise new complaints
- Add complaint details and attachments
- View submitted complaints
- Track complaint status and updates
- View complaint history
- Manage profile information
- Update profile photo

### Admin

- Admin authentication
- Admin dashboard
- View and manage complaints
- Assign complaints to staff
- Manage complaint categories
- Register staff members
- View staff information
- Manage user/profile information
- Monitor complaint progress

### Staff / Agent

- Staff authentication
- View assigned complaints
- Access complaint details
- Update complaint status
- Add updates to complaints
- Work with complaint attachments
- Manage assigned complaints through the workflow

---

## Tech Stack

### Frontend

- React
- Vite
- JavaScript
- HTML
- CSS

### Backend

- Java
- Servlet-based architecture
- Maven
- Apache Tomcat

### Database

- Oracle Live SQL

### Development Tools

- Git & GitHub
- VS Code
- IntelliJ IDEA / Eclipse
- Postman

---

## Project Structure

```text
Complaint_Hub/
│
├── backend/
│   ├── src/
│   │   └── main/
│   │       └── java/
│   │           └── com/
│   │               └── complainthub/
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── utils/
│   │   └── App.jsx
│   ├── package.json
│   └── vite.config.js
│
└── README.md
