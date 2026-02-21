package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.domain.User;

public interface UserRoleService {

    void assignRole(User user, String roleCode);

    void revokeRole(User user, String roleCode);

    void ensureRoleActive(User user, String roleCode);
}
