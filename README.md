# 🎓 Gestion Scolarité - Student Records Management System

![Java](https://img.shields.io/badge/Java-17+-007396?logo=java)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?logo=mysql)
![Swing](https://img.shields.io/badge/Swing-Desktop_UI-FF6B00?logo=java)
![Maven](https://img.shields.io/badge/Maven-Build_Tool-C71A36?logo=apache-maven)

A comprehensive client-server application for managing student records, grades, and academic programs at Université Ibn Khaldoun – Tiaret. This system implements all requirements from the mini-project specification with a professional architecture.

## 🌟 Features

### 👥 Multi-Role System
- **Student Interface**: View personal information, grades, averages, and annual status
- **Teacher Interface**: Create exams, enter grades, view results, calculate subject averages
- **Academic Administration**: Register students, manage programs, view results
- **System Administrator**: User management, backups, statistics, and reports
- **Automated System**: Automatic grade calculation, status determination, bulletin generation

### 📊 Core Functionality
- **Student Management**: Registration with academic background tracking
- **Program Management**: Tronc commun, specializations, orientations, and options
- **Grade Management**: Multiple exam types (controls, exams, projects, TP, TD)
- **Automatic Calculations**: Weighted averages, annual status determination
- **Reporting**: Statistics, bulletins, and backup/restore functionality

## 🏗️ System Architecture

```
Client-Server Architecture
├── Server Layer
│   └── Socket Server (Thread-pooled, concurrent connections)
├── Client Layer (Swing UI)
│   ├── Student Client
│   ├── Teacher Client  
│   ├── Administration Client (Scolarité + Admin)
│   └── Utility Classes
└── Data Layer
    └── MySQL Database (Normalized schema)
```

## 🛠️ Technologies Used

- **Backend**: Java 17, Socket Programming, JDBC
- **Frontend**: Java Swing, AWT
- **Database**: MySQL 8.0, XAMPP
- **Build Tool**: Apache Maven
- **IDE**: NetBeans 25

## 🚀 Installation & Setup

### Prerequisites
- Java JDK 17 or higher
- MySQL Server 8.0
- XAMPP (for Apache/MySQL services)
- Maven 3.8+

### Database Setup
1. Start XAMPP and ensure MySQL service is running
2. Execute the SQL script from `database_setup.sql` (or use the provided schema)
3. Default credentials:
   - **Host**: localhost:3306
   - **Database**: gestion_scolarite2
   - **User**: root
   - **Password**: 123456

### Application Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/gestion_scolarite.git
   cd gestion_scolarite
   ```

2. Build the project with Maven:
   ```bash
   mvn clean install
   ```

3. Start the server first:
   ```bash
   java -cp target/gestion_scolarite-1.0-SNAPSHOT.jar com.mycompany.gestion_scolarite.GestionScolariteServeur
   ```

4. Then launch any client interface:
   ```bash
   # For Student interface
   java -cp target/gestion_scolarite-1.0-SNAPSHOT.jar com.mycompany.gestion_scolarite.ClientEtudiant
   
   # For Teacher interface  
   java -cp target/gestion_scolarite-1.0-SNAPSHOT.jar com.mycompany.gestion_scolarite.ClientEnseignant
   
   # For Administration interface
   java -cp target/gestion_scolarite-1.0-SNAPSHOT.jar com.mycompany.gestion_scolarite.ClientScolarite
   ```

## 🔑 Test Credentials

| Role | Login | Password |
|------|-------|----------|
| **Student** | etud.ahmed | etud123 |
| **Teacher** | prof.dupont | prof123 |
| **Admin** | admin | admin123 |
| **Scolarité** | scolarite1 | scol123 |

## 🎨 User Interface Overview

### Student Dashboard
- 📋 Personal information panel
- 📝 Grade viewing by subject and exam type
- 📊 Annual average calculation
- 🎯 Status display (Admis/Redoublant/Exclu)

### Teacher Interface
- ➕ Exam creation with customizable types and coefficients
- ✏️ Grade entry interface with student lists
- 📝 Exam management (modify/delete)
- 📊 Results viewing and subject average calculation

### Administration Panel
- 🎓 Student registration form
- 📋 Program management interface
- 📈 Annual results dashboard
- 👥 User management (admin mode)
- 💾 Backup/restore functionality

## 🌐 Future Enhancements

- **Web Migration**: Port to WildFly application server with JSF/PrimeFaces
- **REST API**: Convert socket protocol to RESTful services
- **JPA/Hibernate**: Replace JDBC with ORM for better maintainability
- **PDF Export**: Generate official bulletins in PDF format
- **Mobile Support**: Responsive web interface for mobile devices

## 📄 Project Structure

```
src/
├── main/
│   └── java/
│       └── com.mycompany.gestion_scolarite/
│           ├── ClientEtudiant.java       # Student interface
│           ├── ClientEnseignant.java     # Teacher interface  
│           ├── ClientScolarite.java      # Admin/Scolarité interface
│           ├── GestionScolariteServeur.java # Server application
│           └── Utils.java                # Utility helper class
└── resources/
    └── database_setup.sql            # Database schema script
```

## 📧 Contact

**Project Author**: Free
**Email**: your.email@univ-tiaret.dz  
**University**: Université Ibn Khaldoun – Tiaret  
**Department**: Computer Science (ISIL - 3rd Year)  
**Academic Year**: 2025/2026

---

**Mini Project 1 - Gestion de Scolarité**  
*Deadline: November 17, 2025*  
*Consultation: November 18-19, 2025*
