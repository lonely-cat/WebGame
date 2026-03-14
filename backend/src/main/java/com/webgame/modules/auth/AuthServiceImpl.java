package com.webgame.modules.auth;

import com.webgame.modules.auth.AuthDtos.LoginRequest;
import com.webgame.modules.auth.AuthDtos.LoginResponse;
import com.webgame.modules.auth.AuthDtos.RegisterRequest;
import com.webgame.modules.auth.AuthDtos.UserProfileResponse;
import com.webgame.security.JwtTokenProvider;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void register(RegisterRequest request) {
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        return new LoginResponse(
                jwtTokenProvider.generateAccessToken(1L, request.username()),
                jwtTokenProvider.generateRefreshToken(1L, request.username()),
                1L,
                request.username()
        );
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        return new LoginResponse(refreshToken, refreshToken, 1L, "demo");
    }

    @Override
    public UserProfileResponse getCurrentUserProfile() {
        return new UserProfileResponse(1L, "demo", "Demo Player", null);
    }
}
