package com.webgame.modules.game;

import com.webgame.common.BusinessException;
import com.webgame.common.repository.GameRepository;
import com.webgame.modules.game.GameModels.GameConfigEntity;
import com.webgame.modules.game.GameModels.GameConfigSaveRequest;
import com.webgame.modules.game.GameModels.GameEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;

    public GameServiceImpl(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    public List<GameEntity> listOnlineGames() {
        return gameRepository.findOnlineGames();
    }

    @Override
    public GameEntity getGameDetail(String gameCode) {
        return gameRepository.findByCode(gameCode)
                .orElseThrow(() -> new BusinessException("GAME_NOT_FOUND", "game not found"));
    }

    @Override
    public List<GameConfigEntity> getGameConfigs(String gameCode) {
        return gameRepository.findConfigsByGameCode(gameCode);
    }

    @Override
    public void updateGameStatus(String gameCode, Integer status) {
        getGameDetail(gameCode);
        gameRepository.updateStatus(gameCode, status, LocalDateTime.now());
    }

    @Override
    public void updateGameConfig(GameConfigSaveRequest request) {
        GameConfigEntity config = new GameConfigEntity();
        config.setId(System.nanoTime());
        config.gameCode = request.gameCode();
        config.configKey = request.configKey();
        config.configValue = request.configValue();
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        config.setDeleted(false);
        gameRepository.saveConfig(config);
    }
}
