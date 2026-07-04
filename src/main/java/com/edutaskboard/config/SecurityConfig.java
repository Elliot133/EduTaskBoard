package com.edutaskboard.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired private CustomUserDetailsService userDetailsService;

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (req, res, auth) -> {
            String role = auth.getAuthorities().iterator().next().getAuthority();
            if ("ROLE_ADMIN".equals(role))        res.sendRedirect("/admin/dashboard");
            else if ("ROLE_TEACHER".equals(role)) res.sendRedirect("/teacher/dashboard");
            else                                  res.sendRedirect("/student/dashboard");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .userDetailsService(userDetailsService)
            .authorizeHttpRequests(a -> a
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/h2-console/**", "/ws/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/teacher/**").hasRole("TEACHER")
                .requestMatchers("/student/**").hasRole("STUDENT")
                .anyRequest().authenticated()
            )
            .formLogin(f -> f.loginPage("/login").successHandler(successHandler()).failureUrl("/login?error").permitAll())
            .logout(l -> l.logoutSuccessUrl("/login?logout").permitAll())
            .csrf(c -> c.ignoringRequestMatchers("/h2-console/**", "/ws/**"))
            .headers(h -> h.frameOptions(f -> f.sameOrigin()));
            
        return http.build();
    }
}
