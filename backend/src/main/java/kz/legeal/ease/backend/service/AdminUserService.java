package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    Page<UserDto> getAll(Pageable pageable);

    UserDto getById(Long id);

    void revokeLawyerRole(Long userId);

    void blockUser(Long userId);

    void unblockUser(Long userId);
}
