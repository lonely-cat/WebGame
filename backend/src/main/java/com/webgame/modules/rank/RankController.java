package com.webgame.modules.rank;

import com.webgame.common.ApiResponse;
import com.webgame.common.UserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ranks")
public class RankController {

    private final RankService rankService;

    public RankController(RankService rankService) {
        this.rankService = rankService;
    }

    @GetMapping("/global")
    public ApiResponse<?> globalRank(@RequestParam String gameCode) {
        return ApiResponse.success(rankService.getGlobalRank(gameCode));
    }

    @GetMapping("/weekly")
    public ApiResponse<?> weeklyRank(@RequestParam String gameCode) {
        return ApiResponse.success(rankService.getWeeklyRank(gameCode));
    }

    @GetMapping("/me")
    public ApiResponse<?> myRank(@RequestParam String gameCode) {
        return ApiResponse.success(rankService.getUserRank(UserContext.getCurrentUserId(), gameCode));
    }
}
