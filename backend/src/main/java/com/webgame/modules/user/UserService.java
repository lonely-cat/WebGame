package com.webgame.modules.user;

public interface UserService {
    UserEntity getById(Long userId);

    UserEntity findByUsername(String username);

    UserEntity updateProfile(UpdateUserProfileRequest request);

    void updateStatus(Long userId, Integer status);

    record UpdateUserProfileRequest(String nickname, String avatar) {
    }
}
