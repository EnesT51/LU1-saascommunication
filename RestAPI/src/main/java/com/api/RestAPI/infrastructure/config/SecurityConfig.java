package com.api.RestAPI.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.api.RestAPI.infrastructure.security.ApiKeyAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final ApiKeyAuthenticationFilter apiKeyFilter;

    public SecurityConfig(ApiKeyAuthenticationFilter apiKeyFilter) {
        this.apiKeyFilter = apiKeyFilter;
    }


    @Bean
    public SecurityFilterChain securityFilterChain( HttpSecurity http) 
    
    throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            
            ).authorizeHttpRequests(auth -> auth.requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html"
            ).permitAll()
.requestMatchers("/api/message-provider/**").permitAll()
.anyRequest().authenticated()
                    "/actuator/prometheus",
                    "/actuator/health"
            ).permitAll()
.requestMatchers("/api/message-provider/**").permitAll()
.anyRequest().authenticated()
        )
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())

            .addFilterBefore(
                apiKeyFilter,
                UsernamePasswordAuthenticationFilter.class
            );
        return http.build();
    }
}
