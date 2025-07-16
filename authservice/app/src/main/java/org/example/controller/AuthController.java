package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.example.entities.RefreshToken;
import org.example.model.UserDto;
import org.example.response.JwtResponseDTO;
import org.example.service.JwtService;
import org.example.service.RefreshTokenService;
import org.example.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
@RestController
@Tag(name = "Authentication", description = "APIs for user registration, logout, and authentication status")
public class AuthController {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account and returns JWT access token and refresh token upon successful registration"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully", 
                    content = @Content(schema = @Schema(implementation = JwtResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "User already exists", 
                    content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", 
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("auth/v1/signup")
    public ResponseEntity<JwtResponseDTO> createUser(@RequestBody UserDto userDto) {
        // Validate user input
        if (userDto.getUserName() == null || userDto.getUserName().isEmpty()) {
            throw new org.example.exceptions.ValidationException("Username is required");
        }
        if (userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
            throw new org.example.exceptions.ValidationException("Password is required");
        }

        Boolean isSignedUp = userDetailsService.signupUser(userDto);
        if (Boolean.FALSE.equals(isSignedUp)) {
            throw new org.example.exceptions.UserException("User already exists with username: " + userDto.getUserName());
        }

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDto.getUserName());
        String jwtToken = jwtService.generateToken(userDto.getUserName());

        return new ResponseEntity<>(JwtResponseDTO.builder()
                .accessToken(jwtToken)
                .token(refreshToken.getToken())
                .userId(userDto.getUserId())
                .build(), HttpStatus.OK);
    }

    @Operation(
        summary = "Logout user",
        description = "Invalidates the user's refresh token and clears the security context"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful", 
                    content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "401", description = "Not authorized", 
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/auth/v1/logout")
    public ResponseEntity<String> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.example.exceptions.AuthenticationException("User is not authenticated");
        }

        String username = authentication.getName();
        refreshTokenService.deleteByUserName(username);
        SecurityContextHolder.clearContext(); // Clear the security context
        return ResponseEntity.ok("Logged out successfully");
    }


    @Operation(
        summary = "Check authentication status",
        description = "Verifies if the user is authenticated and returns the user ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User is authenticated", 
                    content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "401", description = "User is not authenticated", 
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/auth/v1/ping")
    public ResponseEntity<Map<String,String>> ping() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.example.exceptions.AuthenticationException("User is not authenticated");
        }

        String userId = userDetailsService.getUserByUsername(authentication.getName());
        if (userId == null) {
            throw new org.example.exceptions.ResourceNotFoundException("User", "username", authentication.getName());
        }
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("user_id",userId));
    }

    @Operation(
        summary = "Health check endpoint",
        description = "Simple endpoint to verify the service is up and running"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is healthy", 
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/auth/v1/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }
}
