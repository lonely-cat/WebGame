package com.webgame.modules.auth;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(String username, String password, String nickname) {
    }

    public record LoginRequest(String username, String password) {
    }

    public record RefreshTokenRequest(String refreshToken) {
    }

    public record LoginResponse(String accessToken, String refreshToken, Long userId, String username) {
    }

    public record UserProfileResponse(Long userId, String username, String nickname, String avatar) {
    }
}
