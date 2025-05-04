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
import java.util.UUID;

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

        Users user=userRepository.findByUserName(userName);
        if(user==null){
            log.error("Username not found: "+userName);
            throw new UsernameNotFoundException("Could not find the username "+userName);
        }
        log.info("User Authenticated Successfully..!!!");
        return new CustomUserDetails(user);
    }

    public Users checkIfUserAlreadyExists(UserDto userDto) {
        return userRepository.findByUserName(userDto.getUserName());
    }

    public Boolean signupUser(UserDto userDto) {
        //defining a function to check if email,password is correct
//        if(ValidationUtil.validateUserAttributes(userDto)) {
//            return false;
//        }
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        if(Objects.nonNull(checkIfUserAlreadyExists(userDto))){
            return false;
        }
        String userId=UUID.randomUUID().toString();
        Users userInfo=new Users(userId,userDto.getUserName(),userDto.getPassword(),new HashSet<>());
        userRepository.save(userInfo);

        //sending to kafka
        userInfoProducer.sendEventToKafka(userInfoEventToPublish(userDto,userId));
        return true;
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
