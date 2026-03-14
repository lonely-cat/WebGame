package com.webgame.modules.match;

import com.webgame.modules.match.MatchModels.ActionValidateResult;
import com.webgame.modules.match.MatchModels.GameState;
import com.webgame.modules.match.MatchModels.GameStateView;
import com.webgame.modules.match.MatchModels.MatchInitContext;
import com.webgame.modules.match.MatchModels.MatchResult;
import com.webgame.modules.match.MatchModels.PlayerActionCommand;
import java.util.HashMap;
import org.springframework.stereotype.Component;

public final class RuleEngines {

    private RuleEngines() {
    }

    private abstract static class AbstractRuleEngine implements GameRuleEngine {
        @Override
        public GameState initState(MatchInitContext context) {
            return new GameState(getGameCode(), new HashMap<>());
        }

        @Override
        public ActionValidateResult validateAction(PlayerActionCommand action, GameState state) {
            return new ActionValidateResult(true, "accepted");
        }

        @Override
        public GameState applyAction(PlayerActionCommand action, GameState state) {
            return state;
        }

        @Override
        public boolean isGameOver(GameState state) {
            return false;
        }

        @Override
        public MatchResult buildMatchResult(GameState state) {
            return new MatchResult(getGameCode(), "", null, state.data());
        }

        @Override
        public GameStateView buildStateView(GameState state, Long viewerUserId) {
            return new GameStateView(getGameCode(), state.data());
        }
    }

    @Component
    public static class GomokuRuleEngine extends AbstractRuleEngine {
        @Override
        public String getGameCode() {
            return "gomoku";
        }
    }

    @Component
    public static class ChineseChessRuleEngine extends AbstractRuleEngine {
        @Override
        public String getGameCode() {
            return "chinese-chess";
        }
    }

    @Component
    public static class BlackjackRuleEngine extends AbstractRuleEngine {
        @Override
        public String getGameCode() {
            return "blackjack";
        }
    }

    @Component
    public static class DoodleRuleEngine extends AbstractRuleEngine {
        @Override
        public String getGameCode() {
            return "doodle";
        }
    }

    @Component
    public static class DrawGuessRuleEngine extends AbstractRuleEngine {
        @Override
        public String getGameCode() {
            return "draw-guess";
        }
    }
}
