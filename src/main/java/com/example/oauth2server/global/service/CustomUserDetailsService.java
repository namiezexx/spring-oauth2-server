package com.example.oauth2server.global.service;

import com.example.oauth2server.global.entity.Users;
import com.example.oauth2server.global.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users users = getUserByUsername(username);
        log.info("users::{}", users);
        return User.builder()
                .username(users.getUsername())
                .password(users.getPassword())
                .disabled(!users.getEnabled())
                .accountExpired(!users.getAccountNonExpired())
                .accountLocked(!users.getAccountNonLocked())
                .credentialsExpired(!users.getCredentialsNonExpired())
                .authorities(users.getSimpleAuthorities())
                .build();
    }

    public Users getUserByUsername(String username) throws UsernameNotFoundException {
        Users users = userRepository.findByUsername(username);
        if (users == null) {
            throw new UsernameNotFoundException(username);
        }
        return users;
    }
}
