package com.example.userservice.repository;


import com.example.userservice.entities.UserInfo;
import com.example.userservice.entities.UserInfoDto;
import com.example.userservice.service.UserService;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@EnableJpaRepositories
//CrudRepository<onWhichWeAreGoingToWork,thatTable'sPrimaryKey>
public interface UserRepository extends CrudRepository<UserInfo, String> {
    Optional<UserInfo> findByUserId(String userId);
}
