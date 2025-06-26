package org.example.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.entities.RefreshToken;
import org.example.request.AuthRequestDTO;
import org.example.request.RefreshTokenRequestDTO;
import org.example.response.ErrorResponse;
import org.example.response.JwtResponseDTO;
import org.example.service.JwtService;
import org.example.service.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
@Tag(name = "Token Management", description = "APIs for authentication and token management")
public class TokenController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private JwtService jwtService;

    @Operation(
        summary = "Authenticate user and generate tokens",
        description = "Authenticates a user with username and password, and returns JWT access token and refresh token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication successful", 
                    content = @Content(schema = @Schema(implementation = JwtResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Authentication failed", 
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("auth/v1/login")
    public ResponseEntity<JwtResponseDTO> authenticateAndGetToken(@RequestBody AuthRequestDTO authRequestDTO){
        // Validate request
        if (authRequestDTO.getUsername() == null || authRequestDTO.getUsername().isEmpty()) {
            throw new org.example.exceptions.ValidationException("Username is required");
        }
        if (authRequestDTO.getPassword() == null || authRequestDTO.getPassword().isEmpty()) {
            throw new org.example.exceptions.ValidationException("Password is required");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword())
            );

            if (!authentication.isAuthenticated()) {
                throw new org.example.exceptions.AuthenticationException("Authentication failed for user: " + authRequestDTO.getUsername());
            }

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequestDTO.getUsername());
            return new ResponseEntity<>(JwtResponseDTO.builder()
                    .accessToken(jwtService.generateToken(authRequestDTO.getUsername()))
                    .token(refreshToken.getToken())
                    .userId(refreshToken.getUser().getUserId())
                    .build(), HttpStatus.OK);
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new org.example.exceptions.AuthenticationException("Invalid username or password", e);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(TokenController.class);
    @Operation(
        summary = "Refresh access token",
        description = "Uses a valid refresh token to generate a new access token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully", 
                    content = @Content(schema = @Schema(implementation = JwtResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })

    @PostMapping("auth/v1/refreshToken")
    @ResponseBody
    public ResponseEntity<JwtResponseDTO> refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        log.info("Received refresh token request with token: {}", 
                refreshTokenRequestDTO.getToken() != null ? refreshTokenRequestDTO.getToken().substring(0, Math.min(refreshTokenRequestDTO.getToken().length(), 10)) + "..." : "null");

        if (refreshTokenRequestDTO.getToken() == null || refreshTokenRequestDTO.getToken().isEmpty()) {
            log.warn("Refresh token is null or empty");
            throw new org.example.exceptions.ValidationException("Refresh token is required");
        }

        Optional<RefreshToken> optionalRefreshToken = refreshTokenService.findByToken(refreshTokenRequestDTO.getToken());
        log.info("Found refresh token: {}", optionalRefreshToken.isPresent() ? "present" : "not present");

        try {
            JwtResponseDTO response = optionalRefreshToken
                    .map(token -> {
                        log.info("Verifying token expiration for token ID: {}", token.getId());
                        return refreshTokenService.verifyExpiration(token);
                    })
                    .map(token -> {
                        log.info("Getting user from verified token");
                        return token.getUser();
                    })
                    .map(userInfo -> {
                        log.info("Generating new access token for user: {}", userInfo.getUserName());
                        String accessToken = jwtService.generateToken(userInfo.getUserName());
                        log.info("Successfully generated access token");
                        return JwtResponseDTO.builder()
                                .accessToken(accessToken)
                                .userId(userInfo.getUserId())
                                .token(refreshTokenRequestDTO.getToken())
                                .build();
                    }).orElseThrow(() -> {
                        log.error("Refresh token not found in database");
                        return new org.example.exceptions.TokenException("Refresh token not found. Please login again.");
                    });

            log.info("Successfully refreshed token for user ID: {}", response.getUserId());
            return ResponseEntity.ok(response);
        } catch (org.example.exceptions.TokenException e) {
            log.error("Token exception during refresh: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during token refresh: {}", e.getMessage(), e);
            throw new org.example.exceptions.TokenException("Failed to refresh token: " + e.getMessage(), e);
        }
    }


}
