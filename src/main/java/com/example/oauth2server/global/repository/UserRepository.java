package com.example.oauth2server.global.repository;

import com.example.oauth2server.global.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface UserRepository extends JpaRepository<Users, Long> {

    @Query("SELECT users FROM Users users JOIN FETCH users.authorities WHERE users.username=:username")
    Users findByUsername(String username);
}
