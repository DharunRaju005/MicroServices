package org.example.service;


import jakarta.transaction.Transactional;
import org.apache.catalina.User;
import org.example.entities.RefreshToken;
import org.example.entities.Users;
import org.example.repository.RefreshTokenRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenService {
    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    UserRepository userRepository;

    public RefreshToken createRefreshToken(String username){
        log.info("Creating refresh token for user: {}", username);

        Users userInfoExtracted = userRepository.findByUserName(username);
        if (userInfoExtracted == null) {
            log.error("User not found for username: {}", username);
            throw new org.example.exceptions.ResourceNotFoundException("User", "username", username);
        }

        String tokenValue = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusMillis(1000*60*60); // 1 hour

        RefreshToken refreshToken = RefreshToken.builder()
                .user(userInfoExtracted)
                .token(tokenValue)
                .expireDate(expiryDate)
                .build();

        log.info("Created refresh token with expiry: {}", expiryDate);

        // Check if user already has a refresh token
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser(userInfoExtracted);
        if(existingToken.isPresent()){
            log.info("User already has a refresh token, deleting old token");
            refreshTokenRepository.deleteByUser(userInfoExtracted);
            refreshTokenRepository.flush();
        }

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.info("Saved refresh token with ID: {}", savedToken.getId());
        return savedToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        log.info("Verifying expiration for token ID: {}", token.getId());

        Instant now = Instant.now();
        if(token.getExpireDate().isBefore(now)){
            log.warn("Token expired at {} (current time: {})", token.getExpireDate(), now);
            refreshTokenRepository.delete(token);
            refreshTokenRepository.flush();
            throw new org.example.exceptions.TokenException("Refresh Token is expired. Please re-login.");
        }

        log.info("Token is valid until: {}", token.getExpireDate());
        return token;
    }

    public Optional<RefreshToken> findByToken(String token){
        log.info("Looking up refresh token");

        if (token == null || token.isEmpty()) {
            log.warn("Token is null or empty");
            return Optional.empty();
        }

        Optional<RefreshToken> foundToken = refreshTokenRepository.findByToken(token);
        if (foundToken.isPresent()) {
            log.info("Found token with ID: {}, expiry: {}", foundToken.get().getId(), foundToken.get().getExpireDate());
        } else {
            log.warn("No token found for the provided value");
        }

        return foundToken;
    }

    public void deleteByUserName(String userName){
        log.info("Deleting refresh token for user: {}", userName);

        Users userInfoExtracted = userRepository.findByUserName(userName);
        if (userInfoExtracted == null) {
            log.warn("User not found for username: {}", userName);
            return;
        }

        refreshTokenRepository.deleteByUser(userInfoExtracted);
        log.info("Deleted refresh token for user: {}", userName);
    }

}
