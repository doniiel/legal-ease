package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.dto.UserDto;
import kz.legeal.ease.backend.enums.Role;
import kz.legeal.ease.backend.exception.user.GuardException;
import kz.legeal.ease.backend.mapper.UserMapper;
import kz.legeal.ease.backend.service.*;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final LawyerApplicationService lawyerApplicationService;
    private final NotificationService notificationService;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAll(Pageable pageable) {
        return userService.findAll(pageable)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        return userMapper.toDto(userService.findById(id));
    }

    @Override
    @Transactional
    public void revokeLawyerRole(Long userId) {
        final var currentAdmin = SecurityUtils.getCurrentUserOrThrow();
        final var user = userService.findById(userId);

        guardAgainstSelf(currentAdmin.getId(), userId);
        guardAgainstAdmin(user);

        if (!user.hasRole(Role.LAWYER.name())) {
            log.warn("Revoke LAWYER role failed: user id={} does not have LAWYER role. adminId={}",
                    userId, currentAdmin.getId());
            throw new IllegalStateException("User id=" + userId + " does not have LAWYER role");
        }

        userRoleService.revokeRole(user, Role.LAWYER.name());
        userRoleService.ensureRoleActive(user, Role.USER.name());

        lawyerApplicationService.archiveActiveApplication(user, currentAdmin);
        notificationService.sendLawyerRoleRevoked(user.getEmail());

        log.info("LAWYER role revoked: adminId={} userId={}", currentAdmin.getId(), userId);
    }


    @Override
    @Transactional
    public void blockUser(Long userId) {
        final var currentAdmin = SecurityUtils.getCurrentUserOrThrow();
        final var user = userService.findById(userId);

        guardAgainstSelf(currentAdmin.getId(), userId);
        guardAgainstAdmin(user);

        userService.blockUser(user);
        log.info("User blocked: adminId={} userId={}", currentAdmin.getId(), userId);
    }

    @Override
    @Transactional
    public void unblockUser(Long userId) {
        final var currentAdmin = SecurityUtils.getCurrentUserOrThrow();
        final var user = userService.findById(userId);

        guardAgainstSelf(currentAdmin.getId(), userId);
        guardAgainstAdmin(user);

        userService.unblockUser(user);
        log.info("User unblocked: adminId={} userId={}", currentAdmin.getId(), userId);
    }

    private void guardAgainstSelf(Long adminId, Long targetUserId) {
        if (adminId.equals(targetUserId)) {
            log.warn("Operation denied: admin id={} attempted to modify own account", adminId);
            throw new GuardException("Cannot modify your own account");
        }
    }

    private void guardAgainstAdmin(kz.legeal.ease.backend.domain.User user) {
        if (user.hasRole(Role.ADMIN.name())) {
            log.warn("Operation denied: attempt to modify admin account userId={}", user.getId());
            throw new GuardException("Cannot modify another admin account");
        }
    }
}
