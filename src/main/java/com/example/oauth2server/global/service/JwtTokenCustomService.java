package com.example.oauth2server.global.service;

import com.example.oauth2server.global.entity.Authority;
import com.example.oauth2server.global.entity.Users;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

//@Service
//public class JwtTokenCustomService {
//
////    @Bean
////    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer(UserDetailsService userDetailsService) {
////        return (context) -> {
////            OAuth2TokenType tokenType = context.getTokenType();
////            if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
////                String username = context.getPrincipal().getName();
////                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
////                if (userDetails instanceof Users) {
////                    Users users = (Users) userDetails;
////                    List<Authority> authorities = users.getAuthorities();
////                    context.getClaims().claims((claims) -> {
////                        claims.put("id", users.getId().intValue());
////                        claims.put("authorities", authorities.stream().map(Authority::getAuthority).collect(Collectors.toList()));
////                    });
////                } else {
////                    throw new ClassCastException("UserDetails object is not an instance of Users");
////                }
////            }
////        };
//    }
//}
