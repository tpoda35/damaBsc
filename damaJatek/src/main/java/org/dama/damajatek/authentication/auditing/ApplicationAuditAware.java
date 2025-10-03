package org.dama.damajatek.authentication.auditing;

import org.dama.damajatek.authentication.user.AppUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Provides the current auditor (user ID) for auditing purposes.
 *
 * <p>This class is used by Spring Data JPA's auditing feature to automatically populate
 * audit-related fields such as {@code createdBy} and {@code lastModifiedBy} with the
 * currently authenticated user's ID.</p>
 *
 * <p>The auditor is retrieved from the {@link SecurityContextHolder}, ensuring that
 * only authenticated users' IDs are used for auditing. If no authenticated user is found,
 * an empty {@link Optional} is returned.</p>
 */
public class ApplicationAuditAware implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();
        if (authentication == null ||
            !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken
        ) {
            return Optional.empty();
        }

        AppUser appUserPrincipal = (AppUser) authentication.getPrincipal();
        return Optional.ofNullable(appUserPrincipal.getId());
    }
}
