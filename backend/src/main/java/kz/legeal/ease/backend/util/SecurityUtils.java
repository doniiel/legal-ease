package kz.legeal.ease.backend.util;

import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.jwt.PersonDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<User> getCurrentUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(PersonDetails.class::isInstance)
                .map(PersonDetails.class::cast)
                .map(PersonDetails::getUser);
    }
}