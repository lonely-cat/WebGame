package com.webgame.modules.match;

import com.webgame.modules.match.MatchModels.ActionValidateResult;
import com.webgame.modules.match.MatchModels.GameState;
import com.webgame.modules.match.MatchModels.GameStateView;
import com.webgame.modules.match.MatchModels.MatchInitContext;
import com.webgame.modules.match.MatchModels.MatchResult;
import com.webgame.modules.match.MatchModels.PlayerActionCommand;

public interface GameRuleEngine {
    String getGameCode();

    GameState initState(MatchInitContext context);

    ActionValidateResult validateAction(PlayerActionCommand action, GameState state);

    GameState applyAction(PlayerActionCommand action, GameState state);

    boolean isGameOver(GameState state);

    MatchResult buildMatchResult(GameState state);

    GameStateView buildStateView(GameState state, Long viewerUserId);
}
