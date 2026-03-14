package com.webgame.modules.auth;

import com.webgame.modules.auth.AuthDtos.LoginRequest;
import com.webgame.modules.auth.AuthDtos.LoginResponse;
import com.webgame.modules.auth.AuthDtos.RegisterRequest;
import com.webgame.modules.auth.AuthDtos.UserProfileResponse;

public interface AuthService {
    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse refreshToken(String refreshToken);

    UserProfileResponse getCurrentUserProfile();
}
