package com.devops.devops_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebConfig {
    @Autowired ApiKeyFilter apiKeyFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.POST, "/DevOps").permitAll()
                .anyRequest().denyAll();
        
        return http.build();
    }
}
