package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.domain.UserRole;
import kz.legeal.ease.backend.dto.UserProfileDto;
import kz.legeal.ease.backend.exception.UnauthorizedException;
import kz.legeal.ease.backend.repository.UserRepository;
import kz.legeal.ease.backend.request.UpdateProfileRequest;
import kz.legeal.ease.backend.service.UserProfileService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getProfile() {
        User user = requireCurrentUser();
        return toDto(user);
    }

    @Override
    @Transactional
    public UserProfileDto updateProfile(UpdateProfileRequest request) {
        User user = requireCurrentUser();
        if (request.getFio() != null && !request.getFio().isBlank()) {
            user.setFio(request.getFio());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        userRepository.save(user);
        return toDto(user);
    }

    // ── helpers ───────────────────────────────────────────────

    private User requireCurrentUser() {
        return SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Not authenticated"));
    }

    private UserProfileDto toDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .fio(user.getFio())
                .email(user.getEmail())
                .phone(user.getPhone())
                .iin(user.getIin())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .dateOfBirth(user.getDateOfBirth())
                .role(resolveHighestRole(user))
                .active(user.isActive())
                .build();
    }

    /**
     * Returns the highest active role: ADMIN > LAWYER > USER
     */
    private String resolveHighestRole(User user) {
        var roles = user.getUserRoles().stream()
                .filter(UserRole::isActive)
                .map(ur -> ur.getRole().getCode())
                .toList();
        if (roles.contains("ADMIN"))  return "ADMIN";
        if (roles.contains("LAWYER")) return "LAWYER";
        return "USER";
    }
}
