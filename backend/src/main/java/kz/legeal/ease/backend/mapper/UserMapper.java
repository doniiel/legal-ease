package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.domain.UserRole;
import kz.legeal.ease.backend.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "email",  target = "username")
    @Mapping(target = "firstName",          expression = "java(extractFirstName(user))")
    @Mapping(target = "lastName",           expression = "java(extractLastName(user))")
    @Mapping(target = "role",               expression = "java(extractRole(user))")
    @Mapping(source = "phone", target = "telephone")
    @Mapping(target = "userProfileImageUrl", ignore = true)
    UserDto toDto(User user);

    // ── helpers ──────────────────────────────────────────────────────────────

    /** "Фамилия Имя Отчество" → "Фамилия" (first token = surname in KZ format) */
    default String extractLastName(User user) {
        if (user.getFio() == null || user.getFio().isBlank()) return null;
        String[] parts = user.getFio().trim().split("\\s+");
        return parts[0];
    }

    /** "Фамилия Имя Отчество" → "Имя Отчество" (everything after the surname) */
    default String extractFirstName(User user) {
        if (user.getFio() == null || user.getFio().isBlank()) return null;
        String[] parts = user.getFio().trim().split("\\s+", 2);
        return parts.length > 1 ? parts[1] : null;
    }

    /**
     * Returns the highest-priority active role: ADMIN > LAWYER > USER.
     * Returns "USER" as fallback when no active roles found.
     */
    default String extractRole(User user) {
        if (user.getUserRoles() == null || user.getUserRoles().isEmpty()) return "USER";
        List<String> priority = List.of("ADMIN", "LAWYER", "USER");
        return user.getUserRoles().stream()
                .filter(UserRole::isActive)
                .map(ur -> ur.getRole().getCode())
                .min(Comparator.comparingInt(code -> {
                    int idx = priority.indexOf(code);
                    return idx == -1 ? Integer.MAX_VALUE : idx;
                }))
                .orElse("USER");
    }
}