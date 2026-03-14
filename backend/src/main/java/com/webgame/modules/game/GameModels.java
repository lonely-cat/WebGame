package com.webgame.modules.game;

import com.webgame.common.BaseEntity;

public final class GameModels {

    private GameModels() {
    }

    public static class GameEntity extends BaseEntity {
        public String gameCode;
        public String gameName;
        public String gameType;
        public String icon;
        public Integer status;
        public Boolean supportsMultiplayer;
        public String routePath;
    }

    public static class GameConfigEntity extends BaseEntity {
        public String gameCode;
        public String configKey;
        public String configValue;
    }

    public record GameSaveRequest(String gameCode, String gameName, String gameType, String routePath) {
    }

    public record GameUpdateRequest(String gameCode, String gameName, Integer status) {
    }

    public record GameConfigSaveRequest(String gameCode, String configKey, String configValue) {
    }
}
