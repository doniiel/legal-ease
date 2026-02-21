package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.dto.UserDto;
import kz.legeal.ease.backend.mapper.UserMapper;
import kz.legeal.ease.backend.service.*;
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
        final var admin = requireCurrentAdmin();
        final var user = userService.findById(userId);

        guardAgainstSelf(admin.getId(), userId);
        guardAgainstAdmin(user);

        if (!user.hasRole(Role.LAWYER.name())) {
            throw new IllegalStateException("User id=" + userId + " does not have LAWYER role");
        }

        // 1. Убрать роль LAWYER
        userRoleService.revokeRole(user, Role.LAWYER.name());

        // 2. Убедиться что USER роль активна
        userRoleService.ensureRoleActive(user, Role.USER.name());

        // 3. Архивировать активную заявку
        lawyerApplicationService.archiveActiveApplication(user, admin);

        // 4. Уведомить
        notificationService.sendLawyerRoleRevoked(user.getEmail());

        log.info("Admin id={} revoked LAWYER role from user id={}", admin.getId(), userId);
    }



}
