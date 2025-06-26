package org.example.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;

import java.security.Key;
import java.util.function.Function;
import java.util.*;

@Service
public class JwtService {
    public static final String SECRET = "9f8b3e7d1a2c4f6d8e9f0b1c3d5e7a6f9b0c2d4e6f8a1b3c4d6e7f9a0b1c2d3e";
//
//    we have claims it is kind of ds
//    Claims = User Information stored inside JWTs


    public String extractUsername(String token) {
        if (token == null || token.isEmpty()) {
            throw new org.example.exceptions.TokenException("JWT token cannot be null or empty");
        }

        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            throw new org.example.exceptions.TokenException("Failed to extract username from token", e);
        }
    }

    public Date extractExpiration(String token){
        return extractClaim(token,Claims::getExpiration);
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims=extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        if (token == null || token.isEmpty()) {
            throw new org.example.exceptions.TokenException("JWT token cannot be null or empty");
        }

        try {
            return Jwts
                    .parser()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new org.example.exceptions.TokenException("JWT token has expired", e);
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            throw new org.example.exceptions.TokenException("Unsupported JWT token", e);
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            throw new org.example.exceptions.TokenException("Malformed JWT token", e);
        } catch (io.jsonwebtoken.SignatureException e) {
            throw new org.example.exceptions.TokenException("Invalid JWT signature", e);
        } catch (Exception e) {
            throw new org.example.exceptions.TokenException("Failed to parse JWT token", e);
        }
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        if (token == null || token.isEmpty()) {
            throw new org.example.exceptions.TokenException("JWT token cannot be null or empty");
        }

        if (userDetails == null) {
            throw new org.example.exceptions.ValidationException("UserDetails cannot be null");
        }

        try {
            final String username = extractUsername(token);

            if (!username.equals(userDetails.getUsername())) {
                throw new org.example.exceptions.TokenException("Token username doesn't match UserDetails username");
            }

            if (isTokenExpired(token)) {
                throw new org.example.exceptions.TokenException("Token has expired");
            }

            return true;
        } catch (org.example.exceptions.TokenException e) {
            throw e;
        } catch (Exception e) {
            throw new org.example.exceptions.TokenException("Token validation failed", e);
        }
    }

    public String generateToken(String username) {
        if (username == null || username.isEmpty()) {
            throw new org.example.exceptions.ValidationException("Username cannot be null or empty for token generation");
        }

        try {
            Map<String, Object> claims = new HashMap<>();
            return createToken(claims, username);
        } catch (Exception e) {
            throw new org.example.exceptions.TokenException("Failed to generate JWT token", e);
        }
    }

    private String createToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*10))//10min of expiry
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    private Key getSignKey() {
        byte[] keyBytes= Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
