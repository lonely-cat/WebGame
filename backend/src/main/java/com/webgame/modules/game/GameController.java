package com.webgame.modules.game;

import com.webgame.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public ApiResponse<?> listGames() {
        return ApiResponse.success(gameService.listOnlineGames());
    }

    @GetMapping("/{gameCode}")
    public ApiResponse<?> getGameDetail(@PathVariable String gameCode) {
        return ApiResponse.success(gameService.getGameDetail(gameCode));
    }

    @GetMapping("/{gameCode}/configs")
    public ApiResponse<?> getConfigs(@PathVariable String gameCode) {
        return ApiResponse.success(gameService.getGameConfigs(gameCode));
    }
}
