package com.webgame.modules.auth;

import com.webgame.common.BusinessException;
import com.webgame.common.repository.UserRepository;
import com.webgame.common.UserContext;
import com.webgame.modules.auth.AuthDtos.LoginRequest;
import com.webgame.modules.auth.AuthDtos.LoginResponse;
import com.webgame.modules.auth.AuthDtos.RegisterRequest;
import com.webgame.modules.auth.AuthDtos.UserProfileResponse;
import com.webgame.modules.user.UserEntity;
import com.webgame.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthServiceImpl(JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new BusinessException("USERNAME_EXISTS", "username already exists");
        }
        UserEntity user = new UserEntity();
        user.setUsername(request.username());
        user.setNickname(request.nickname());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(1);
        user.setCreateTime(java.time.LocalDateTime.now());
        user.setUpdateTime(java.time.LocalDateTime.now());
        user.setDeleted(false);
        userRepository.insert(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException("AUTH_FAILED", "username or password is incorrect"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("AUTH_FAILED", "username or password is incorrect");
        }
        user.setLastLoginTime(java.time.LocalDateTime.now());
        user.setUpdateTime(java.time.LocalDateTime.now());
        userRepository.update(user);
        return new LoginResponse(
                jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername()),
                jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername()),
                user.getId(),
                user.getUsername()
        );
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("TOKEN_INVALID", "refresh token is invalid");
        }
        Long userId = jwtTokenProvider.parseToken(refreshToken).get("userId", Long.class);
        String username = jwtTokenProvider.parseToken(refreshToken).getSubject();
        return new LoginResponse(
                jwtTokenProvider.generateAccessToken(userId, username),
                jwtTokenProvider.generateRefreshToken(userId, username),
                userId,
                username
        );
    }

    @Override
    public UserProfileResponse getCurrentUserProfile() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("UNAUTHORIZED", "login required");
        }
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "user not found"));
        return new UserProfileResponse(user.getId(), user.getUsername(), user.getNickname(), user.getAvatar());
    }
}
