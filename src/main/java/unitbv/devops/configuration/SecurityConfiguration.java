package unitbv.devops.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import unitbv.devops.security.JwtAuthenticationFilter;

import java.util.Arrays;

/**
 * Configurație Security relaxată complet pentru generare date și screenshots
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private Environment env;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // --- LOGICĂ PENTRU CI/CD (Phase 11) ---
        if (Arrays.asList(env.getActiveProfiles()).contains("test")) {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .cors(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        // --- CONFIGURAȚIA RELAXATĂ (Pentru Screenshots/Demo) ---
        http
                .authorizeHttpRequests(authz -> authz
                        // 👇 AICI ESTE MODIFICAREA CHEIE: .permitAll() la tot ce e /api/**
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers(
                                "/actuator/health/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/internal/**",
                                "/ws/**"
                        ).permitAll()
                        // 👇 Lăsăm liber accesul peste tot temporar
                        .anyRequest().permitAll()
                )
                // Păstrăm filtrul ca să nu crape dependențele, dar regulile de sus permit accesul oricum
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable);

        return http.build();
    }
}