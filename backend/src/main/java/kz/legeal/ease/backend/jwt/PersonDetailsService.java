package kz.legeal.ease.backend.jwt;

import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.domain.UserRole;
import kz.legeal.ease.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private static final List<String> ROLE_PRIORITY = List.of("ADMIN", "LAWYER", "USER");

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        final var user = findByEmailOrThrow(email);
        // ✅ Исправлено: было findFirst() без приоритета — недетерминированно
        final var userRole = findRoleByPriority(user, email);
        return new PersonDetails(user, userRole);
    }

    @Transactional
    public UserDetails loadUserByUserId(Long userId) throws UsernameNotFoundException {
        final var user = findByIdOrThrow(userId);
        final var userRole = findRoleByPriority(user, String.valueOf(userId));
        return new PersonDetails(user, userRole);
    }

    @Transactional
    public UserDetails loadUserByUserIdAndRole(Long userId, String roleCode)
            throws UsernameNotFoundException {

        final var user = findByIdOrThrow(userId);

        final var userRole = user.getUserRoles().stream()
                .filter(UserRole::isActive)
                .filter(ur -> ur.getRole().getCode().equals(roleCode))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Role '" + roleCode + "' not active for user id=" + userId));

        return new PersonDetails(user, userRole);
    }

    @Transactional
    public UserDetails loadUserByUsernameWithHighestRole(String email) {
        final var user = findByEmailOrThrow(email);
        final var userRole = findRoleByPriority(user, email);
        return new PersonDetails(user, userRole);
    }
    private UserRole findRoleByPriority(User user, String identifier) {
        return ROLE_PRIORITY.stream()
                .flatMap(priority -> user.getUserRoles().stream()
                        .filter(UserRole::isActive)
                        .filter(ur -> ur.getRole().getCode().equals(priority)))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No active role for user: " + identifier));
    }

    private User findByEmailOrThrow(String email) {
        return userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private User findByIdOrThrow(Long userId) {
        return userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found id=" + userId));
    }
}