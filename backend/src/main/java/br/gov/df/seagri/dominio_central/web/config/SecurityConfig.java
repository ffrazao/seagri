package br.gov.df.seagri.dominio_central.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            
            // Desabilita CSRF pois nossa API é Stateless e não usa Cookies de sessão
            .csrf(csrf -> csrf.disable())
            
            // Define que o Spring não deve criar sessão HTTP (Stateless)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Exige que qualquer requisição esteja autenticada
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            
            // Configura a aplicação como um Resource Server que aceita JWT do Keycloak
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {})); 

        return http.build();
    }
}