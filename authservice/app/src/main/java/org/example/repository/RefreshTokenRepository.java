package org.example.repository;

import org.example.entities.RefreshToken;
import org.example.entities.Users;
import org.example.service.RefreshTokenService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(Users users);
    Optional<RefreshToken> findByUser(Users users);
}
