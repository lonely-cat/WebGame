package com.webgame.modules.match;

import com.webgame.modules.match.MatchModels.MatchResult;
import com.webgame.modules.match.MatchModels.PlayerActionCommand;

public interface MatchService {
    Long createMatchFromRoom(Long roomId);

    void startMatch(Long matchId);

    void finishMatch(Long matchId, MatchResult result);

    void abortMatch(Long matchId);

    void submitPlayerAction(PlayerActionCommand command);

    Object getMatchSnapshot(Long matchId);
}
