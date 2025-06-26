package org.example.service;

import org.example.entities.Users;
import org.example.eventProducer.UserInfoEvent;
import org.example.eventProducer.UserInfoProducer;
import org.example.model.UserDto;
import org.example.repository.UserRepository;
import org.example.utils.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.*;

@Component
@AllArgsConstructor
@Data
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final UserInfoProducer userInfoProducer;

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        if (userName == null || userName.isEmpty()) {
            log.error("Username is null or empty");
            throw new org.example.exceptions.ValidationException("Username cannot be empty");
        }

        Users user = userRepository.findByUserName(userName);
        if (user == null) {
            log.error("Username not found: " + userName);
            throw new org.example.exceptions.ResourceNotFoundException("User", "username", userName);
        }

        log.info("User found successfully: {}", userName);
        return new CustomUserDetails(user);
    }

    public Users checkIfUserAlreadyExists(UserDto userDto) {
        return userRepository.findByUserName(userDto.getUserName());
    }

    public String getUserByUsername(String username){
        return Optional.of(userRepository.findByUserName(username)).map(Users::getUserId).orElse(null);
    }


    public Boolean signupUser(UserDto userDto) {
        // Validate user input
        if (userDto == null) {
            throw new org.example.exceptions.ValidationException("User data cannot be null");
        }

        if (userDto.getUserName() == null || userDto.getUserName().isEmpty()) {
            throw new org.example.exceptions.ValidationException("Username is required");
        }

        if (userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
            throw new org.example.exceptions.ValidationException("Password is required");
        }

        // Check if user already exists
        if (Objects.nonNull(checkIfUserAlreadyExists(userDto))) {
            throw new org.example.exceptions.UserException("User already exists with username: " + userDto.getUserName());
        }

        try {
            // Encode password
            userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));

            // Generate user ID
            String userId = UUID.randomUUID().toString();
            userDto.setUserId(userId);

            // Create and save user
            Users userInfo = new Users(userId, userDto.getUserName(), userDto.getPassword(), new HashSet<>());
            userRepository.save(userInfo);

            // Send to Kafka
            userInfoProducer.sendEventToKafka(userInfoEventToPublish(userDto, userId));

            log.info("User registered successfully: {}", userDto.getUserName());
            return true;
        } catch (Exception e) {
            log.error("Error during user registration: {}", e.getMessage());
            throw new RuntimeException("Error during user registration", e);
        }
    }

    private UserInfoEvent userInfoEventToPublish(UserDto userInfoDto, String userId){
        return UserInfoEvent.builder()
                .userId(userId)
                .firstName(userInfoDto.getFirstName())
                .lastName(userInfoDto.getLastName())
                .email(userInfoDto.getEmail())
                .phoneNumber(userInfoDto.getPhoneNumber()).build();

    }
}
