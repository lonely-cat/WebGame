package com.webgame.modules.user;

import com.webgame.common.BusinessException;
import com.webgame.common.repository.UserRepository;
import com.webgame.common.UserContext;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserEntity getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "user not found"));
    }

    @Override
    public UserEntity findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "user not found"));
    }

    @Override
    public UserEntity updateProfile(UpdateUserProfileRequest request) {
        Long userId = UserContext.getCurrentUserId();
        UserEntity user = getById(userId);
        user.setNickname(request.nickname());
        user.setAvatar(request.avatar());
        user.setUpdateTime(LocalDateTime.now());
        userRepository.update(user);
        return user;
    }

    @Override
    public void updateStatus(Long userId, Integer status) {
        UserEntity user = getById(userId);
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userRepository.update(user);
    }
}
