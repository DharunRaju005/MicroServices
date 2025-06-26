package org.example.auth;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.service.JwtService;
import org.example.service.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter
{
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private final JwtService jwtService;
    @Autowired
    private final UserDetailsServiceImpl userDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException
    {
        try {
            String path = request.getServletPath();
            log.debug("Processing request for path: {}", path);


            String authHeader = request.getHeader("Authorization");

            // Check if Authorization header is present and has correct format
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // Continue the filter chain without authentication
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);
            String username;

            try {
                username = jwtService.extractUsername(token);
            } catch (org.example.exceptions.TokenException e) {
                // Handle token extraction error
                handleAuthenticationException(response, e);
                return;
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Validate token
                    jwtService.validateToken(token, userDetails);

                    // Create authentication object
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } catch (org.example.exceptions.TokenException | org.example.exceptions.ResourceNotFoundException e) {
                    handleAuthenticationException(response, e);
                    return;
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // Handle any unexpected exceptions
            handleAuthenticationException(response, new org.example.exceptions.AuthenticationException("Authentication failed", e));
        }
    }

    private void handleAuthenticationException(HttpServletResponse response, Exception exception) throws IOException {
        log.error("Authentication exception occurred: {}", exception.getMessage(), exception);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        String errorMessage = exception.getMessage() != null ? exception.getMessage() : "Authentication failed";
        String jsonResponse = "{\"error\":\"Unauthorized\",\"message\":\"" + errorMessage + "\"}";

        log.debug("Sending error response: {}", jsonResponse);
        response.getWriter().write(jsonResponse);
    }
}
