package Vaccination.Management.System.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class JpaConfigTest {

    private final AuditorAware<Long> auditor = new JpaConfig().auditorProvider();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsUserId_whenAuthenticated() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("42", null, AuthorityUtils.createAuthorityList("ROLE_MEDICAL_STAFF")));

        assertThat(auditor.getCurrentAuditor()).contains(42L);
    }

    @Test
    void returnsEmpty_whenNoAuthentication() {
        assertThat(auditor.getCurrentAuditor()).isEmpty();
    }

    @Test
    void returnsEmpty_whenAnonymous() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        assertThat(auditor.getCurrentAuditor()).isEmpty();
    }
}
