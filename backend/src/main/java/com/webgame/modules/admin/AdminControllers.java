package com.webgame.modules.admin;

import com.webgame.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

public final class AdminControllers {

    private AdminControllers() {
    }

    public record NoticeSaveRequest(String title, String content, Integer status) {
    }

    @RestController
    @RequestMapping("/api/admin/users")
    public static class AdminUserController {
        @GetMapping
        public ApiResponse<?> pageUsers() {
            return ApiResponse.success("todo", null);
        }

        @GetMapping("/{userId}")
        public ApiResponse<?> getUserDetail(@PathVariable Long userId) {
            return ApiResponse.success("todo", userId);
        }

        @PostMapping("/{userId}/ban")
        public ApiResponse<Void> banUser(@PathVariable Long userId) {
            return ApiResponse.success("banned", null);
        }

        @PostMapping("/{userId}/unban")
        public ApiResponse<Void> unbanUser(@PathVariable Long userId) {
            return ApiResponse.success("unbanned", null);
        }
    }

    @RestController
    @RequestMapping("/api/admin/assets")
    public static class AdminAssetController {
        @GetMapping
        public ApiResponse<?> pageUsersAsset() {
            return ApiResponse.success("todo", null);
        }

        @PostMapping("/coin")
        public ApiResponse<Void> adjustUserCoin(@RequestBody Object request) {
            return ApiResponse.success("adjusted", null);
        }

        @PostMapping("/score")
        public ApiResponse<Void> adjustUserScore(@RequestBody Object request) {
            return ApiResponse.success("adjusted", null);
        }
    }

    @RestController
    @RequestMapping("/api/admin/games")
    public static class AdminGameController {
        @GetMapping
        public ApiResponse<?> pageGames() {
            return ApiResponse.success("todo", null);
        }

        @PostMapping
        public ApiResponse<Void> saveGame(@RequestBody Object request) {
            return ApiResponse.success("saved", null);
        }

        @PostMapping("/update")
        public ApiResponse<Void> updateGame(@RequestBody Object request) {
            return ApiResponse.success("updated", null);
        }
    }

    @RestController
    @RequestMapping("/api/admin/dashboard")
    public static class AdminDashboardController {
        @GetMapping("/overview")
        public ApiResponse<?> overview() {
            return ApiResponse.success("todo", null);
        }

        @GetMapping("/games")
        public ApiResponse<?> gameStats() {
            return ApiResponse.success("todo", null);
        }

        @GetMapping("/online")
        public ApiResponse<?> onlineStats() {
            return ApiResponse.success("todo", null);
        }
    }

    @RestController
    @RequestMapping("/api/admin/records")
    public static class AdminRecordController {
        @GetMapping
        public ApiResponse<?> pageRecords() {
            return ApiResponse.success("todo", null);
        }

        @GetMapping("/{matchCode}")
        public ApiResponse<?> matchDetail(@PathVariable String matchCode) {
            return ApiResponse.success("todo", matchCode);
        }
    }

    @RestController
    @RequestMapping("/api/admin/notices")
    public static class AdminNoticeController {
        @PostMapping
        public ApiResponse<Void> createNotice(@RequestBody NoticeSaveRequest request) {
            return ApiResponse.success("created", null);
        }

        @PostMapping("/update")
        public ApiResponse<Void> updateNotice(@RequestBody NoticeSaveRequest request) {
            return ApiResponse.success("updated", null);
        }

        @PostMapping("/{noticeId}/delete")
        public ApiResponse<Void> deleteNotice(@PathVariable Long noticeId) {
            return ApiResponse.success("deleted", null);
        }

        @GetMapping
        public ApiResponse<?> listNotices() {
            return ApiResponse.success("todo", null);
        }
    }
}
