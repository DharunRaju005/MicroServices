# Spring Security Flow Documentation

## Overview
This document explains the security flow of the authentication service, detailing how Spring Security is implemented with JWT (JSON Web Token) authentication. The application provides user registration, authentication, and token management capabilities with a stateless security model.

## Authentication Flow

### 1. User Registration (Signup)
- **Endpoint**: `/auth/v1/signup`
- **Controller**: `AuthController`
- **Process**:
  1. User submits registration details (username, password, etc.)
  2. `UserDetailsServiceImpl` checks if the user already exists
  3. If not, password is encoded using `BCryptPasswordEncoder`
  4. User is saved to the database
  5. A refresh token is created with 1-hour validity
  6. An access token (JWT) is generated with 10-minute validity
  7. Both tokens are returned to the client
  8. User information is published to Kafka for other services

### 2. User Authentication (Login)
- **Endpoint**: `/auth/v1/login`
- **Controller**: `TokenController`
- **Process**:
  1. User submits credentials (username and password)
  2. `AuthenticationManager` authenticates the user using `DaoAuthenticationProvider`
  3. `UserDetailsServiceImpl` loads user details from the database
  4. Password is verified using `BCryptPasswordEncoder`
  5. If authentication is successful, a refresh token is created
  6. An access token (JWT) is generated
  7. Both tokens are returned to the client

### 3. Request Authorization
- **Component**: `JwtAuthFilter`
- **Process**:
  1. For each request (except public endpoints), the filter extracts the JWT from the Authorization header
  2. The token is validated using `JwtService`
  3. User details are loaded from the database
  4. If the token is valid, an `Authentication` object is created and set in the `SecurityContext`
  5. The request is allowed to proceed to the controller

### 4. Token Refresh
- **Endpoint**: `/auth/v1/refreshToken`
- **Controller**: `TokenController`
- **Process**:
  1. Client sends the refresh token
  2. `RefreshTokenService` verifies the token exists and hasn't expired
  3. If valid, a new access token is generated
  4. The new access token and existing refresh token are returned to the client

### 5. Logout
- **Endpoint**: `/auth/v1/logout`
- **Controller**: `AuthController`
- **Process**:
  1. The refresh token for the authenticated user is deleted from the database
  2. The security context is cleared
  3. Client should discard the tokens

## Key Components

### 1. SecurityConfig
- Configures the security filter chain
- Defines public and protected endpoints
- Sets up stateless session management
- Configures authentication provider and manager
- Registers the JWT authentication filter

### 2. JwtAuthFilter
- Extends `OncePerRequestFilter` to intercept each request
- Skips authentication for public endpoints
- Extracts and validates JWT tokens
- Sets up the security context for authenticated requests

### 3. JwtService
- Generates JWT tokens with claims and expiration (10 minutes)
- Validates tokens by checking username and expiration
- Extracts claims from tokens
- Signs tokens with a secret key using HS256 algorithm

### 4. UserDetailsServiceImpl
- Implements Spring Security's `UserDetailsService`
- Loads user details from the database for authentication
- Handles user registration with password encoding
- Checks if users already exist

### 5. RefreshTokenService
- Creates refresh tokens with longer expiration (1 hour)
- Verifies token expiration
- Manages token storage and retrieval
- Handles token deletion during logout

### 6. AuthenticationManager and AuthenticationProvider
- `AuthenticationManager` orchestrates the authentication process
- `DaoAuthenticationProvider` authenticates users against the database
- Uses `UserDetailsService` to load user information
- Uses `PasswordEncoder` to verify passwords

## Security Flow Diagram

```
Client                                                                 Server
  |                                                                      |
  |  1. Registration Request (username, password)                        |
  | --------------------------------------------------------------------> 
  |                                                                      | UserDetailsServiceImpl.signupUser()
  |                                                                      | - Encode password
  |                                                                      | - Save user to database
  |                                                                      | - Create refresh token
  |                                                                      | - Generate JWT token
  |  2. Response (JWT token, refresh token)                              |
  | <--------------------------------------------------------------------
  |                                                                      |
  |  3. Login Request (username, password)                               |
  | --------------------------------------------------------------------> 
  |                                                                      | AuthenticationManager.authenticate()
  |                                                                      | - Validate credentials
  |                                                                      | - Create refresh token
  |                                                                      | - Generate JWT token
  |  4. Response (JWT token, refresh token)                              |
  | <--------------------------------------------------------------------
  |                                                                      |
  |  5. API Request + Authorization: Bearer {jwt_token}                  |
  | --------------------------------------------------------------------> 
  |                                                                      | JwtAuthFilter.doFilterInternal()
  |                                                                      | - Extract token
  |                                                                      | - Validate token
  |                                                                      | - Set SecurityContext
  |                                                                      | 
  |                                                                      | Controller.endpoint()
  |                                                                      | - Process request
  |  6. API Response                                                     |
  | <--------------------------------------------------------------------
  |                                                                      |
  |  7. Token Refresh Request (refresh token)                            |
  | --------------------------------------------------------------------> 
  |                                                                      | RefreshTokenService.verifyExpiration()
  |                                                                      | - Validate refresh token
  |                                                                      | - Generate new JWT token
  |  8. Response (new JWT token, same refresh token)                     |
  | <--------------------------------------------------------------------
  |                                                                      |
  |  9. Logout Request + Authorization: Bearer {jwt_token}               |
  | --------------------------------------------------------------------> 
  |                                                                      | AuthController.logout()
  |                                                                      | - Delete refresh token
  |                                                                      | - Clear SecurityContext
  | 10. Logout Response                                                  |
  | <--------------------------------------------------------------------
```

## Security Considerations

1. **Token Expiration**:
   - Access tokens expire after 10 minutes
   - Refresh tokens expire after 1 hour
   - Expired tokens are automatically rejected

2. **Password Security**:
   - Passwords are encoded using BCrypt before storage
   - Raw passwords are never stored in the database

3. **Stateless Authentication**:
   - No session state is maintained on the server
   - Each request must include a valid JWT token
   - Security context is created for each request

4. **Token Storage**:
   - Access tokens should be stored securely by clients (e.g., memory)
   - Refresh tokens are stored in the database with user association
   - Each user can have only one active refresh token

5. **Public Endpoints**:
   - `/auth/v1/login` - For user login
   - `/auth/v1/signup` - For user registration
   - `/auth/v1/refreshToken` - For token refresh
   - `/health` - For service health check

6. **Protected Endpoints**:
   - All other endpoints require a valid JWT token
   - Authorization header format: `Bearer {jwt_token}`

## Conclusion

The authentication service implements a robust security mechanism using Spring Security and JWT tokens. The stateless nature of JWT authentication makes it suitable for microservices architecture, while the refresh token mechanism provides a balance between security and user experience by limiting the lifespan of access tokens without requiring frequent re-authentication.