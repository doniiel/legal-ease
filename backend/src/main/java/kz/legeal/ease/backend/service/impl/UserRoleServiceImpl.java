package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.domain.UserRole;
import kz.legeal.ease.backend.exception.role.RoleNotFoundException;
import kz.legeal.ease.backend.repository.RoleRepository;
import kz.legeal.ease.backend.repository.UserRepository;
import kz.legeal.ease.backend.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new RoleNotFoundException(roleCode));

        final var alreadyAssigned = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getCode().equals(roleCode) && ur.isActive());

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

    @Override
    @Transactional
    public void revokeRole(User user, String roleCode) {
        final var hasActiveRole = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getCode().equals(roleCode) && ur.isActive());

        if (!hasActiveRole) {
            log.warn("User id={} does not have active role {}", user.getId(), roleCode);
            return;
        }

        user.getUserRoles().stream()
                .filter(ur -> ur.getRole().getCode().equals(roleCode) && ur.isActive())
                .forEach(ur -> ur.setActive(false));

        userRepository.save(user);
        log.info("Revoked role {} from user id={}", roleCode, user.getId());
    }

    @Override
    @Transactional
    public void ensureRoleActive(User user, String roleCode) {
        final var existing = user.getUserRoles().stream()
                .filter(ur -> ur.getRole().getCode().equals(roleCode))
                .findFirst();

        if (existing.isPresent()) {
            if (!existing.get().isActive()) {
                existing.get().setActive(true);
                userRepository.save(user);
                log.info("Re-activated role {} for user id={}", roleCode, user.getId());
            }
        } else {
            assignRole(user, roleCode);
        }
    }
}
