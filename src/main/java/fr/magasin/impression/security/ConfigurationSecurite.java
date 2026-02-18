package fr.magasin.impression.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ConfigurationSecurite {

    @Bean
    public SecurityFilterChain filtreSecurite(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(regles -> regles
                        .requestMatchers("/api/etat", "/api/depots/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().denyAll()
                )
                .httpBasic(httpBasic -> {})
                .build();
    }
}
