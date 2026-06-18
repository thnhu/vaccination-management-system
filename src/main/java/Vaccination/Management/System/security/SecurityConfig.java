package Vaccination.Management.System.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                "/vaccination/swagger-ui/**",
//                                "/vaccination/v3/api-docs/**",
//                                "/swagger-ui/**",
//                                "/v3/api-docs/**"
//                        ).permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/vaccines/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/vaccines/**").hasRole("FACILITY_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/vaccines/**").hasRole("FACILITY_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/facilities/**").hasRole("FACILITY_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/facilities/**").hasRole("FACILITY_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/facilities/**").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/facilities/*/batches").hasAnyRole("FACILITY_ADMIN", "MEDICAL_STAFF")
                        .requestMatchers(HttpMethod.PATCH, "/batches/*/recall").hasRole("FACILITY_ADMIN")
                        .requestMatchers("/citizens/me").hasRole("CITIZEN")
                        .requestMatchers("/vaccination-records/**").hasRole("MEDICAL_STAFF")
                        .requestMatchers(HttpMethod.POST, "/vaccination-records/**").hasRole("MEDICAL_STAFF")
                        .requestMatchers(HttpMethod.GET,  "/citizens/*/vaccination-records").hasAnyRole("CITIZEN", "MEDICAL_STAFF")
                        .requestMatchers("/appointments/today").hasRole("MEDICAL_STAFF")
                        .requestMatchers(HttpMethod.GET, "/appointments/*/history").hasAnyRole("MEDICAL_STAFF", "CITIZEN")
                        .requestMatchers(HttpMethod.POST, "/appointments").hasRole("CITIZEN")
                        .requestMatchers(HttpMethod.GET,  "/appointments/my").hasRole("CITIZEN")
                        .requestMatchers(HttpMethod.PATCH, "/appointments/*/confirm").hasRole("MEDICAL_STAFF")
                        .requestMatchers(HttpMethod.POST, "/advisor/chat").hasRole("CITIZEN")
                        .requestMatchers(HttpMethod.GET, "/advisor/recommendations").hasRole("CITIZEN")
                        .requestMatchers(HttpMethod.GET, "/advisor/available-slots").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(401);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"code\":1008,\"message\":\"Unauthorized\"}");
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(403);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"code\":1006,\"message\":\"Access denied\"}");
                        })
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}