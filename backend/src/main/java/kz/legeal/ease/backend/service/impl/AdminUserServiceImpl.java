package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.domain.User;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
        final var admin = requireCurrentAdmin();
        final var user = userService.findById(userId);

        guardAgainstSelf(admin.getId(), userId);
        guardAgainstAdmin(user);

        if (!user.hasRole(Role.LAWYER.name())) {
            throw new IllegalStateException("User id=" + userId + " does not have LAWYER role");
        }

        userRoleService.revokeRole(user, Role.LAWYER.name());
        userRoleService.ensureRoleActive(user, Role.USER.name());

        lawyerApplicationService.archiveActiveApplication(user, admin);
        notificationService.sendLawyerRoleRevoked(user.getEmail());

        log.info("Admin id={} revoked LAWYER role from user id={}", admin.getId(), userId);
    }


    @Override
    @Transactional
    public void blockUser(Long userId) {
        final var admin = requireCurrentAdmin();
        final var user = userService.findById(userId);

        guardAgainstSelf(admin.getId(), userId);
        guardAgainstAdmin(user);

        userService.blockUser(user);
        log.info("Admin id={} blocked user id={}", admin.getId(), userId);
    }

    @Override
    @Transactional
    public void unblockUser(Long userId) {
        final var admin = requireCurrentAdmin();
        final var user = userService.findById(userId);

        guardAgainstSelf(admin.getId(), userId);
        guardAgainstAdmin(user);

        userService.unblockUser(user);
        log.info("Admin id={} unblocked user id={}", admin.getId(), userId);
    }

    private User requireCurrentAdmin() {
        return SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
    }

    private void guardAgainstSelf(Long adminId, Long targetUserId) {
        if (adminId.equals(targetUserId)) {
            throw new GuardException("Cannot modify your own account");
        }
    }

    private void guardAgainstAdmin(kz.legeal.ease.backend.domain.User user) {
        if (user.hasRole(Role.ADMIN.name())) {
            throw new GuardException("Cannot modify another admin account");
        }
    }
}
