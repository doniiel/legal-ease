package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.domain.UserRole;
import kz.legeal.ease.backend.repository.RoleRepository;
import kz.legeal.ease.backend.repository.UserRepository;
import kz.legeal.ease.backend.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public void assignRole(User user, String roleCode) {
        final var role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleCode));

        final var alreadyAssigned = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().equals(role) && ur.isActive());

        if (alreadyAssigned) {
            log.info("User {} already has role {}", user.getId(), role.getCode());
            return;
        }

        final var userRole = UserRole.builder()
                .user(user)
                .role(role)
                .active(true)
                .assignedAt(LocalDateTime.now())
                .build();

        user.getUserRoles().add(userRole);

        userRepository.save(user);

        log.info("Assigned role {} to user {}", role.getCode(), user.getId());
    }
}
