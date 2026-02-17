package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.domain.User;

public interface UserRoleService {

    void assignRole(User user, String roleCode);
}
