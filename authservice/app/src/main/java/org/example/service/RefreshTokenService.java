package org.example.service;


import org.apache.catalina.User;
import org.example.entities.RefreshToken;
import org.example.entities.Users;
import org.example.repository.RefreshTokenRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    UserRepository userRepository;

    public RefreshToken createRefreshToken(String username){
        Users userInfoExtracted = userRepository.findByUserName(username);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(userInfoExtracted)
                .token(UUID.randomUUID().toString())
                .expireDate(Instant.now().plusMillis(600000))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpireDate().isBefore(Instant.now())){
            refreshTokenRepository.delete(token);
            throw new RuntimeException(token.getToken()+ " Refresh Token is expired please re-login");
        }
        return token;
    }

    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }
}
