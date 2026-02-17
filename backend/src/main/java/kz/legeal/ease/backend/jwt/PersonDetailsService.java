package kz.legeal.ease.backend.jwt;

import kz.legeal.ease.backend.repository.UserRepository;
import kz.legeal.ease.backend.service.UserRoleService;
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
    private final UserRoleService userRoleService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        final var user = userRepository.findByEmailAndDeletedFalseAndActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));
        final var userRole = userRoleService.findActiveByUser(user)
                .orElseThrow(() -> new UsernameNotFoundException("Active role for user " + email + " not found"));
        return new PersonDetails(user, userRole);
    }

    @Transactional
    public UserDetails loadUserByUserId(Long userId) throws UsernameNotFoundException {
        final var user = userRepository.findByIdAndDeletedFalseAndActiveTrue(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User with id " + userId + " not found"));

        final var userRole = userRoleService.findActiveByUser(user)
                .orElseThrow(() -> new UsernameNotFoundException("Active role for user id " + userId + " not found"));

        return new PersonDetails(user, userRole);
    }
}
