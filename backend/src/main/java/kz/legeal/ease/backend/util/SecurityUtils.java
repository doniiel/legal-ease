package kz.legeal.ease.backend.util;

import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.exception.UnauthorizedException;
import kz.legeal.ease.backend.jwt.PersonDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof PersonDetails personDetails) {
            return Optional.of(personDetails.getUser());
        }

        return Optional.empty();
    }

    public static User getCurrentUserOrThrow() {
        return getCurrentUser()
                .orElseThrow(UnauthorizedException::new);
    }
}