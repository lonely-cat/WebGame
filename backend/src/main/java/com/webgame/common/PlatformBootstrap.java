package com.webgame.common;

import com.webgame.common.repository.AssetRepository;
import com.webgame.common.repository.GameRepository;
import com.webgame.common.repository.UserRepository;
import com.webgame.modules.asset.AssetModels.UserAssetEntity;
import com.webgame.modules.game.GameModels.GameConfigEntity;
import com.webgame.modules.game.GameModels.GameEntity;
import com.webgame.modules.user.UserEntity;
import java.time.LocalDateTime;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PlatformBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final GameRepository gameRepository;
    private final PasswordEncoder passwordEncoder;

    public PlatformBootstrap(UserRepository userRepository, AssetRepository assetRepository, GameRepository gameRepository,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
        this.gameRepository = gameRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedAdmin();
        seedGames();
    }

    private void seedAdmin() {
        if (userRepository.existsAdminUser()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        UserEntity user = new UserEntity();
        user.setUsername("admin");
        user.setNickname("Administrator");
        user.setPassword(passwordEncoder.encode("admin123"));
        user.setStatus(1);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setDeleted(false);
        long userId = userRepository.insert(user);

        UserAssetEntity asset = new UserAssetEntity();
        asset.userId = userId;
        asset.score = 0;
        asset.coin = 1000;
        asset.level = 1;
        asset.exp = 0;
        asset.setCreateTime(now);
        asset.setUpdateTime(now);
        asset.setDeleted(false);
        assetRepository.insert(asset);
    }

    private void seedGames() {
        if (gameRepository.hasAnyGames()) {
            return;
        }
        saveGame("gomoku", "Gomoku", "board", true, "/games/gomoku", 1);
        saveGame("chinese-chess", "Chinese Chess", "board", true, "/games/chinese-chess", 2);
        saveGame("blackjack", "Blackjack", "poker", true, "/games/blackjack", 3);
        saveGame("doodle", "Doodle", "drawing", true, "/games/doodle", 4);
        saveGame("draw-guess", "Draw and Guess", "social", true, "/games/draw-guess", 5);

        saveConfig("gomoku", "maxPlayers", "2");
        saveConfig("draw-guess", "maxPlayers", "8");
        saveConfig("draw-guess", "roundSeconds", "90");
    }

    private void saveGame(String gameCode, String gameName, String gameType, boolean supportsMultiplayer,
                          String routePath, int sortNo) {
        LocalDateTime now = LocalDateTime.now();
        GameEntity game = new GameEntity();
        game.gameCode = gameCode;
        game.gameName = gameName;
        game.gameType = gameType;
        game.icon = null;
        game.status = 1;
        game.supportsMultiplayer = supportsMultiplayer;
        game.routePath = routePath;
        game.setCreateTime(now);
        game.setUpdateTime(now);
        game.setDeleted(false);
        gameRepository.insert(game, sortNo);
    }

    private void saveConfig(String gameCode, String configKey, String configValue) {
        LocalDateTime now = LocalDateTime.now();
        GameConfigEntity config = new GameConfigEntity();
        config.gameCode = gameCode;
        config.configKey = configKey;
        config.configValue = configValue;
        config.setCreateTime(now);
        config.setUpdateTime(now);
        config.setDeleted(false);
        gameRepository.saveConfig(config);
    }
}
