# Alumni Mentoring Platform

A comprehensive web-based platform that connects students with alumni mentors for guidance and career development.

## 🎯 Overview

The Alumni Mentoring Platform facilitates meaningful connections between current students and successful alumni. Students can request mentorship from alumni, engage in real-time chat conversations, and build professional relationships that extend beyond graduation.

## ✨ Features

### 🔐 Authentication & User Management
- **Role-based Access Control**: Students, Alumni, and Admin roles
- **Secure Authentication**: Session-based authentication with password hashing
- **User Registration**: Separate registration flows for students and alumni
- **Profile Management**: Comprehensive user profiles with contact information

### 🤝 Mentorship System
- **Mentor Requests**: Students can request mentorship from available alumni
- **Request Management**: Alumni can accept/reject mentorship requests
- **Status Tracking**: Real-time status updates for mentorship requests
- **Role-based Permissions**: Different capabilities based on user role

### 💬 Real-time Communication
- **WebSocket Chat**: Instant messaging between mentors and mentees
- **Chat Rooms**: Dedicated chat rooms for each mentorship relationship
- **Message History**: Persistent message storage and retrieval
- **Optimistic UI**: Messages appear immediately for better user experience

## 🏗️ Architecture

### Backend (Java EE)
- **Jakarta EE 11**: Modern enterprise Java platform
- **JAX-RS**: RESTful web services
- **JPA/EclipseLink**: Object-relational mapping
- **WebSockets**: Real-time communication
- **JMS**: Asynchronous message processing
- **Session Management**: HTTP session-based authentication

### Frontend ( JavaScript)
- **Responsive Design**: Mobile-friendly interface
- **Modular Architecture**: Separate modules for different features
- **API Client**: Centralized HTTP request handling
- **Real-time Updates**: WebSocket integration for live chat

### Database
- **MySQL**: Relational database for data persistence
- **JPA Entities**: Object-oriented data modeling
- **Single Table Inheritance**: Efficient user type management

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- MySQL 8.0 or higher
- GlassFish 8
- Maven 3.6 or higher

### Installation

1. **Database Setup**
   ```sql
   -- Create MySQL database
   -- Create Database
    CREATE DATABASE IF NOT EXISTS alumni_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

     USE alumni_db;

    -- Drop tables if they exist (for clean setup)
    DROP TABLE IF EXISTS messages;
    DROP TABLE IF EXISTS mentor_requests;
    DROP TABLE IF EXISTS alumni_skills;
    DROP TABLE IF EXISTS users;

     -- Users table (with single-table inheritance)
    CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       DTYPE VARCHAR(31) NOT NULL,  -- Discriminator column (changed from user_type)
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       full_name VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Student specific fields
                       enrollment_year INT NULL,
                       major VARCHAR(200) NULL,

    -- Alumni specific fields
                       graduation_year INT NULL,
                       company VARCHAR(200) NULL,
                       position VARCHAR(200) NULL,
                       bio VARCHAR(1000) NULL,
                       available_for_mentoring BOOLEAN DEFAULT TRUE,
                       linkedin VARCHAR(200) NULL,

                       INDEX idx_email (email),
                       INDEX idx_dtype (DTYPE),  -- Changed index name
                       INDEX idx_role (role)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    -- Alumni Skills table (for many-to-many relationship)
    CREATE TABLE alumni_skills (
                               alumni_id BIGINT NOT NULL,
                               skill VARCHAR(255) NOT NULL,

                               PRIMARY KEY (alumni_id, skill),
                               FOREIGN KEY (alumni_id) REFERENCES users(id) ON DELETE CASCADE,
                               INDEX idx_skill (skill)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    -- Mentor Requests table
    CREATE TABLE mentor_requests (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 student_id BIGINT NOT NULL,
                                 alumni_id BIGINT NOT NULL,
                                 message VARCHAR(1000),
                                 status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                 FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
                                 FOREIGN KEY (alumni_id) REFERENCES users(id) ON DELETE CASCADE,
                                 INDEX idx_student (student_id),
                                 INDEX idx_alumni (alumni_id),
                                 INDEX idx_status (status),
                                 INDEX idx_created_at (created_at)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    -- Messages table
   CREATE TABLE messages (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          sender_id BIGINT NOT NULL,
                          recipient_id BIGINT NOT NULL,
                          content VARCHAR(2000) NOT NULL,
                          read_status BOOLEAN DEFAULT FALSE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                          FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
                          FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE,
                          INDEX idx_sender (sender_id),
                          INDEX idx_recipient (recipient_id),
                          INDEX idx_read_status (read_status),
                          INDEX idx_created_at (created_at),
                          INDEX idx_conversation (sender_id, recipient_id)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


2. **Database Configuration**
   Update `src/main/resources/META-INF/persistence.xml`:
   ```xml
   <persistence-unit name="AlumniPU" transaction-type="JTA">
       <jta-data-source>jdbc/AlumniDSS</jta-data-source>
       <class>com.hlayiseko.AlumniMentoring.entity.User</class>
       <class>com.hlayiseko.AlumniMentoring.entity.Student</class>
       <class>com.hlayiseko.AlumniMentoring.entity.AlumniProfile</class>
       <class>com.hlayiseko.AlumniMentoring.entity.MentorRequest</class>
       <class>com.hlayiseko.AlumniMentoring.entity.ChatRoom</class>
       <class>com.hlayiseko.AlumniMentoring.entity.Message</class>
       <properties>
           <property name="jakarta.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/alumni_db"/>
           <property name="jakarta.persistence.jdbc.user" value="alumni_user"/>
           <property name="jakarta.persistence.jdbc.password" value="your_password"/>
           <property name="jakarta.persistence.jdbc.driver" value="com.mysql.cj.jdbc.Driver"/>
           <property name="eclipselink.ddl-generation" value="create-or-extend-tables"/>
           <property name="eclipselink.logging.level" value="FINE"/>
       </properties>
   </persistence-unit>
   ```

3. **Build the Application**
   ```bash
   mvn clean package
   ```

4. **Deploy to Server**
   - Deploy the generated WAR file to your GlassFish
   - Access the application at `http://localhost:8080/AlumniMentoring`

### Configuration

#### Database Configuration
The application uses MySQL with the following default settings:
- **Database**: `alumni_db`
- **Host**: `localhost:3306`
- **Driver**: MySQL Connector/J 8.0.33

Update the database connection in `DbInitializer.java`:
```java
@DataSourceDefinition(
    name = "jdbc/AlumniDSS",
    className = "com.mysql.cj.jdbc.MysqlDataSource",
    url = "jdbc:mysql://localhost:3306/alumni_db",
    user = "your_username",
    password = "your_password",
    properties = {
        "useSSL=false",
        "allowPublicKeyRetrieval=true"
    }
)
```


## 📁 Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/hlayiseko/studentmanagementsystem/
│   │       ├── entity/          # JPA entities
│   │       ├── rest/            # REST endpoints
│   │       ├── service/         # Business logic
│   │       ├── security/        # Authentication
│   │       └── websocket/       # WebSocket handlers
│   ├── resources/
│   │   └── META-INF/
│   │       ├── beans.xml        # CDI configuration
│   │       └── persistence.xml  # Database configuration
│   └── webapp/
│       ├── css/                 # Stylesheets
│       ├── js/                  # JavaScript modules
│       ├── *.html              # Web pages
│       └── WEB-INF/
│           └── web.xml         # Web configuration
└── test/                       # Test files
```

## 🔧 API Endpoints

### Authentication
- `POST /auth/login` - User login
- `POST /auth/register` - User registration
- `GET /auth/current-user` - Get current user info
- `POST /auth/logout` - User logout

### Alumni Management
- `GET /alumni` - List all alumni
- `POST /alumni` - Create alumni profile (Admin only)
- `PUT /alumni/{id}` - Update alumni profile

### Mentorship Requests
- `GET /requests` - Get mentorship requests (role-based filtering)
- `POST /requests` - Create mentorship request (Students only)
- `PUT /requests/{id}/status` - Update request status (Alumni/Admin only)

### Chat System
- `GET /chat/rooms` - Get user's chat rooms
- `GET /chat/rooms/{id}/messages` - Get chat messages
- `WebSocket /chat/{roomId}/{userId}` - Real-time messaging

## 🔄 How the App Works

### Application Flow

#### 1. **User Registration & Authentication**
```
User visits app → Register/Login → Session created → Role-based dashboard
```

**Steps:**
1. User accesses `http://localhost:8080/AlumniMentoring`
2. Redirected to login page if not authenticated
3. User can register as Student or Alumni with different fields
4. Login creates HTTP session with user role and ID
5. Dashboard shows different content based on user role

#### 2. **Mentorship Request Flow**
```
Student → Browse Alumni → Send Request → Alumni Reviews → Accept/Reject → Chat Enabled
```

**Student Side:**
1. Student visits "Find Mentor" page
2. Views list of available alumni mentors
3. Clicks "Request Mentorship" on desired alumni
4. Writes message and submits request
5. Request appears in "Mentorship Request" page as "PENDING"

**Alumni Side:**
1. Alumni visits "Mentorship Request" page
2. Sees requests from students
3. Can accept or reject requests
4. Accepted requests enable chat functionality
5. Rejected requests are marked as "REJECTED"

#### 3. **Chat System Flow**
```
Request Accepted → Chat Room Created → WebSocket Connection → Real-time Messaging
```

**Process:**
1. When alumni accepts a request, a chat room is automatically created
2. Both student and alumni can access the chat
3. WebSocket connection established for real-time messaging
4. Messages are stored in database and displayed in chat interface
5. Unread message counts are tracked and displayed

#### 4. **Role-Based Access Control**

**Students Can:**
- Browse available alumni mentors
- Send mentorship requests
- View their own requests
- Chat with accepted mentors
- Update their profile

**Alumni Can:**
- View mentorship requests sent to them
- Accept/reject requests
- Chat with students (after accepting)
- Update their profile
- Cannot browse other alumni

**Admin Can:**
- Do everything alumni can do
- View all requests in the system
- Manage user accounts


## 🎨 User Interface

### Pages
- **Login** (`login.html`) - User authentication
- **Register** (`register.html`) - User registration
- **Dashboard** (`dashboard.html`) - Main application interface
- **Profile** (`profile.html`) - User profile management
- **Requests** (`requests.html`) - Mentorship request management
- **Chat** (`chat.html`) - Real-time messaging interface


## 🔒 Security

### Authentication
- Session-based authentication
- Password hashing using PBKDF2
- Role-based access control
- Session invalidation on logout

### Authorization
- REST endpoint protection
- Role-based method access
- WebSocket participant validation
- Client-side access control

## 🧪 Testing

### Manual Testing
1. **Registration**: Create accounts for different user types
2. **Login**: Test authentication with valid/invalid credentials
3. **Mentorship**: Create and manage mentorship requests
4. **Chat**: Test real-time messaging functionality
5. **Email**: Verify email notifications in console logs

### API Testing
Use tools like Postman or browser dev tools to test REST endpoints.

### Test Scenarios

#### Complete User Journey
1. **Register as Student**
   - Fill out student registration form
   - Verify account creation
   - Check welcome email in console

2. **Register as Alumni**
   - Fill out alumni registration form
   - Verify account creation
   - Check welcome email in console

3. **Student Requests Mentorship**
   - Login as student
   - Browse alumni list
   - Send mentorship request
   - Verify request appears in alumni's request list

4. **Alumni Manages Requests**
   - Login as alumni
   - View pending requests
   - Accept a request
   - Verify chat room is created

5. **Real-time Chat**
   - Both users access chat
   - Send messages back and forth
   - Verify messages appear in real-time
   - Check unread message counts






