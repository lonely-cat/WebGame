package com.webgame.modules.user;

import com.webgame.common.ApiResponse;
import com.webgame.common.UserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserEntity> getCurrentUser() {
        return ApiResponse.success(userService.getById(UserContext.getCurrentUserId()));
    }

    @PutMapping("/me")
    public ApiResponse<UserEntity> updateProfile(@RequestBody UserService.UpdateUserProfileRequest request) {
        return ApiResponse.success(userService.updateProfile(request));
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserEntity> getUserDetail(@PathVariable Long userId) {
        return ApiResponse.success(userService.getById(userId));
    }
}
