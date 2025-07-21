# MicroServices Project

## Overview

This project is a **microservices-based system** implemented primarily in Java (Spring Boot), with additional services in Python (Flask). The system demonstrates secure, scalable, and event-driven architecture using **Apache Kafka** for inter-service communication. It also integrates **Kong** as an API Gateway for centralized routing, authentication, and request management.


## Technology Stack

| Layer             | Technology                       | Purpose                                              |
|-------------------|----------------------------------|------------------------------------------------------|
| **API Gateway**   | Kong + Custom Lua Plugins        | Centralized endpoint for all APIs, request routing, authentication, rate limiting, and custom JWT/refresh token validation |
| **Backend**       | Spring Boot (Java), Flask (Python)| Microservices (auth, user, expense, data service)    |
| **Asynchronous Messaging** | Apache Kafka            | Event-driven, asynchronous communication between services |
| **Database**      | MySQL                            | Persistent storage for user and expense data         |
| **Security**      | Spring Security, JWT, Kong Auth Plugins | Authentication, authorization, token management     |
| **Build Tools**   | Maven/Gradle, pip                | Dependency management and builds                     |
| **Containerization** | Docker              | Service packaging and orchestration                  |




## Architecture

### Microservices Breakdown

- **authservice** (Java/Spring Boot): Handles authentication (signup, login, logout, token management).
- **userservice** (Java/Spring Boot): Manages user-specific data and logic.
- **expenseservice** (Java/Spring Boot): Manages expenses, receives data via REST and Kafka.
- **dsservice** (Python/Flask): Acts as a data service, processes messages and interacts with Kafka.
- **Kong API Gateway**: Provides a single entry point for all client requests, handling routing, security, and rate-limiting.

### Inter-Service Communication

- **Kafka** is used as the message broker for all asynchronous communication. For example:
  - When a user registers, `authservice` publishes a user event to Kafka.
  - Other services (like `userservice` and `expenseservice`) listen to these Kafka topics to update their own state or trigger workflows.
  - The `dsservice` can receive and process messages, then send results to other services through Kafka topics.

## Security Architecture
- **Kong API Gateway**: Centralized security, routing, and rate limiting.
- **Custom Lua plugins for Auth**: JWT and refresh token validation at the gateway.
- **Spring Security**: All Java services use Spring Security for authentication and authorization.
- **JWT Tokens**: Used for stateless authentication across all protected endpoints.
- **Refresh Token**: Allows users to get a new JWT without re-authenticating.
## Scenario: User Journey

### 1. User Registration
- **Entry Point**: The user sends a registration request (username, password, and other details) to the `authservice` via `/auth/v1/signup`.
- **Flow**:
  - The request is validated (for required fields).
  - Passwords are securely hashed (`BCryptPasswordEncoder`).
  - User is created in the database.
  - **Kafka Integration**: User information is published to Kafka for downstream services to consume.
  - The user receives a **JWT access token** and a **refresh token**.

### 2. User Authentication (Login)
- **Entry Point**: The user submits credentials to `/auth/v1/login`.
- **Flow**:
  - Credentials are authenticated.
  - On success, new JWT and refresh tokens are issued.

### 3. Using the Application
- **API Requests**: The user can now access protected endpoints in other services (e.g., `userservice`, `expenseservice`) by including the JWT token in headers.
- **Token Refresh**: If the JWT expires, the user can refresh it using the refresh token.
- **Logout**: The user can log out, which invalidates the refresh token and clears the security context.



## Kong API Gateway Integration

### Role and Benefits

- **Centralized API Management**: All external API requests are routed through Kong.
- **Authentication & Authorization**: Kong enforces authentication at the gateway level before requests reach internal services.
- **Custom Authentication with Lua**: 
  - Kong is extended with custom Lua plugins to validate JWT access tokens and refresh tokens.
  - The Lua code checks the validity of the access token on each request.
  - For token refresh, Kong can pass the refresh token to `authservice` endpoints for validation and JWT issuance.

### Token Roles

- **Access Token (JWT)**
  - Short-lived (e.g., 10 minutes).
  - Used for authenticating user requests to protected APIs via Kong.
  - Validated by Kong’s custom Lua middleware before routing requests.

- **Refresh Token**
  - Longer-lived (e.g., 1 hour).
  - Used to obtain a new access token without re-authenticating.
  - Passed to `/auth/v1/refreshToken` via Kong for validation. On success, a new JWT is returned.

### How the Custom Auth Works

- Custom Lua plugins in Kong:
  - Extract the Authorization header.
  - Validate the JWT’s signature, expiration, and claims.
  - Optionally extract and forward the user context (userId, roles) to backend services.
  - For expired access tokens, direct the client to use the refresh token endpoint.


## Service Interaction Diagram (Textual)

```
[User] ---> [Kong API Gateway (with Lua Auth)] ---> [authservice], [userservice], [expenseservice], [dsservice] (via internal routing)
                                                     |
                                                   [Kafka Broker]
                                                     |
                 -------------------------------------------------------------
                 |                         |                                |
          [userservice]             [expenseservice]                  [dsservice]

- Kong authenticates all requests before forwarding.
- authservice publishes user events to Kafka.
- userservice/expenseservice listen and react to relevant events.
- dsservice can publish processed data/events to Kafka for other services.
```



## How Kafka Enables Loose Coupling

- Each service can publish or subscribe to Kafka topics.
- Services do **not** call each other directly; they communicate via events.
- This allows:
  - Independent scaling
  - Failure isolation
  - Easy integration of new services



## API Endpoints and Their Purpose

Below is a summary of the main endpoints provided by each microservice in this project, including their purpose and how they fit into the overall system.

### Kong API Gateway

- **Purpose:**  
  Acts as a single entry point for all client requests, handling routing, authentication, and rate limiting. All the endpoints below are exposed through Kong, which enforces custom authentication and forwards requests to the appropriate microservice.



### AuthService (Authentication & Authorization)

| Endpoint                | Method | Purpose                                                                                              |
|-------------------------|--------|------------------------------------------------------------------------------------------------------|
| `/auth/v1/signup`       | POST   | Register a new user. Validates input, hashes password, saves user, issues JWT & refresh token, publishes event to Kafka. |
| `/auth/v1/login`        | POST   | Authenticate user credentials. Issues a new JWT and refresh token on success.                        |
| `/auth/v1/refreshToken` | POST   | Exchange a valid refresh token for a new JWT access token. Used when the access token has expired.   |
| `/auth/v1/logout`       | POST   | Logs out the user by invalidating the refresh token and clearing the security context.               |
| `/auth/v1/ping`         | GET    | Health/auth check endpoint. Verifies token and returns user ID if authenticated.                     |



### UserService (User Data Management)

| Endpoint                | Method | Purpose                                                                                              |
|-------------------------|--------|------------------------------------------------------------------------------------------------------|
| `/users/v1/{userId}`    | GET    | Retrieve user details by user ID. (Extendable for profile, settings, etc.)                           |
| `/users/v1/`            | POST   | Create a new user (if separate from auth) or update user profile.                                    |
| `/users/v1/`            | PUT    | Update user data.                                                                                    |
| `/users/v1/`            | DELETE | Delete user account.                                                                                 |

> **Note:** Endpoints may vary based on actual implementation—customize as needed for your business logic.

---

### ExpenseService (Expense Tracking)

| Endpoint                    | Method | Purpose                                                                                              |
|-----------------------------|--------|------------------------------------------------------------------------------------------------------|
| `/expenses/v1/`             | POST   | Create a new expense for a user. Validates and stores expense, supports ingestion via REST or Kafka. |
| `/expenses/v1/user/{userId}`| GET    | Retrieve all expenses for a specific user.                                                           |
| `/expenses/v1/{expenseId}`  | GET    | Retrieve a specific expense by ID.                                                                   |
| `/expenses/v1/`             | GET    | List all expenses (admin or user scope).                                                             |
| `/expenses/v1/{expenseId}`  | DELETE | Delete an expense by ID.                                                                             |



### DSService (Data Service / ML / Processing)

| Endpoint                       | Method | Purpose                                                                                          |
|---------------------------------|--------|--------------------------------------------------------------------------------------------------|
| `/v1/ds/message/`               | POST   | Ingest a message (text or data) for processing. The service processes and forwards results via Kafka. |
| `/`                             | GET    | Health check endpoint (returns a simple message).                                                |



## Endpoint Workflow Examples

- **User Registration & Onboarding:**
  1. Client POSTs to `/auth/v1/signup` (via Kong).
  2. On success, receives JWT and refresh token.
  3. User event published to Kafka for other services.

- **Login & Token Refresh:**
  1. Client POSTs to `/auth/v1/login` to get tokens.
  2. Uses JWT for protected routes (e.g., `/expenses/v1/user/{userId}`).
  3. If JWT expires, POST to `/auth/v1/refreshToken` with refresh token to get a new JWT.

- **Expense Management:**
  1. Client POSTs to `/expenses/v1/` to add an expense.
  2. GET `/expenses/v1/user/{userId}` to fetch all user expenses.

- **ML/Data Processing:**
  1. Client POSTs to `/v1/ds/message/` with data for processing.
  2. DSService processes and publishes results to Kafka.



## Getting Started

1. **Clone the Repository**
    ```bash
    git clone https://github.com/DharunRaju005/MicroServices.git
    ```

2. **Set Up Kafka, MySQL, and Kong**
    - Ensure Kafka and MySQL are running (local or Docker).
    - Deploy Kong (official Docker image recommended).
    - Add custom Lua plugins for authentication to Kong’s plugin folder and configure routes/services.
    - Update config files for correct hostnames/ports.

3. **Build and Run Services**
    - For Java services:
      ```bash
      cd authservice
      ./mvnw spring-boot:run
      # Repeat for userservice, expenseservice
      ```
    - For Python service:
      ```bash
      cd dsservice
      python3 src/app/__init__.py
      ```

4. **Test Endpoints**
    - Use Postman or curl to interact with signup, login, expense APIs, etc, via the Kong API Gateway.



## Extending the System

- Add more services by subscribing/publishing to Kafka topics.
- Integrate with other databases or caches as needed.
- Enhance the security model (roles, permissions).
- Add more Kong plugins or customize existing ones for audits, rate limiting, etc.



## Contributing

Pull requests are welcome! Please create issues for any bugs or feature requests.




## Acknowledgements

- Spring Boot Documentation
- Apache Kafka Documentation
- Flask Documentation
- Kong API Gateway Documentation

---

**For more details and code samples, see individual service directories and their README files (e.g., `expenseservice/README.md`).**
