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

@Service
@RequiredArgsConstructor
public class PersonDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

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

    private UserRole findActiveRole(User user, String identifier) {
        return user.getUserRoles().stream()
                .filter(UserRole::isActive)
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No active role for user: " + identifier));
    }
}
