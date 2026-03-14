package com.webgame.modules.game;

import com.webgame.modules.game.GameModels.GameConfigEntity;
import com.webgame.modules.game.GameModels.GameConfigSaveRequest;
import com.webgame.modules.game.GameModels.GameEntity;
import java.util.List;

public interface GameService {
    List<GameEntity> listOnlineGames();

    GameEntity getGameDetail(String gameCode);

    List<GameConfigEntity> getGameConfigs(String gameCode);

    void updateGameStatus(String gameCode, Integer status);

    void updateGameConfig(GameConfigSaveRequest request);
}
