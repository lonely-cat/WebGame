package com.webgame.modules.match;

import com.webgame.common.BaseEntity;
import java.util.Map;

public final class MatchModels {

    private MatchModels() {
    }

    public static class GameMatchEntity extends BaseEntity {
        public String matchCode;
        public String gameCode;
        public Long roomId;
        public Integer status;
        public Long winnerUserId;
    }

    public static class MatchPlayerEntity extends BaseEntity {
        public Long matchId;
        public Long userId;
        public Integer seatNo;
        public String result;
        public Integer score;
    }

    public record MatchInitContext(Long roomId, String gameCode) {
    }

    public record PlayerActionCommand(String gameCode, String matchCode, Long userId, String actionType,
                                      Map<String, Object> payload) {
    }

    public record GameState(String gameCode, Map<String, Object> data) {
    }

    public record ActionValidateResult(boolean valid, String message) {
    }

    public record MatchResult(String gameCode, String matchCode, Long winnerUserId, Map<String, Object> summary) {
    }

    public record GameStateView(String gameCode, Map<String, Object> visibleState) {
    }
}
