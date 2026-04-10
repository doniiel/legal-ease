package kz.legeal.ease.backend.util;

import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.exception.ForbiddenException;
import kz.legeal.ease.backend.exception.UnauthorizedException;
import kz.legeal.ease.backend.jwt.PersonDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
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

    public static User requireCurrentUser() {
        return getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Authentication required"));
    }

    public static void requireRole(User user, String role) {
        if (!user.hasRole(role)) {
            throw new ForbiddenException("Access denied: requires role " + role);
        }
    }

    public static void requireAnyRole(User user, String... roles) {
        boolean hasAny = Arrays.stream(roles).anyMatch(user::hasRole);
        if (!hasAny) {
            throw new ForbiddenException("Access denied: requires one of " + Arrays.toString(roles));
        }
    }
}