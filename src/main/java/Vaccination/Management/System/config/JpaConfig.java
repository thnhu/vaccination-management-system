package Vaccination.Management.System.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaConfig {

    /**
     * Supplies the current user's id for {@code @CreatedBy} fields.
     * The authenticated principal's username is the user id (see CustomUserDetailsService).
     * Empty when there is no authenticated user (e.g. registration, scheduled jobs).
     */
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                return Optional.empty();
            }
            try {
                return Optional.of(Long.parseLong(auth.getName()));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        };
    }
}
