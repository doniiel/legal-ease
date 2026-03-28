package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.dto.UserProfileDto;
import kz.legeal.ease.backend.request.UpdateProfileRequest;

public interface UserProfileService {
    UserProfileDto getProfile();
    UserProfileDto updateProfile(UpdateProfileRequest request);
}
