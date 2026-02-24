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
        final var user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        final var userRole = findActiveRole(user, email);
        return new PersonDetails(user, userRole);
    }

    @Transactional
    public UserDetails loadUserByUserId(Long userId) throws UsernameNotFoundException {
        final var user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        final var userRole = findActiveRole(user, String.valueOf(userId));
        return new PersonDetails(user, userRole);
    }

    @Transactional
    public UserDetails loadUserByUserIdAndRole(Long userId, String roleCode)
            throws UsernameNotFoundException {

        final var user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        final var userRole = user.getUserRoles().stream()
                .filter(UserRole::isActive)
                .filter(ur -> ur.getRole().getCode().equals(roleCode))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Role " + roleCode + " not active for user: " + userId));

        return new PersonDetails(user, userRole);
    }


    @Transactional
    public UserDetails loadUserByUsernameWithHighestRole(String email) {
        final var user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        final var userRole = ROLE_PRIORITY.stream()
                .flatMap(priority -> user.getUserRoles().stream()
                        .filter(UserRole::isActive)
                        .filter(ur -> ur.getRole().getCode().equals(priority)))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("No active role for: " + email));

        return new PersonDetails(user, userRole);
    }

    private UserRole findActiveRole(User user, String identifier) {
        return user.getUserRoles().stream()
                .filter(UserRole::isActive)
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No active role for user: " + identifier));
    }
}
