package com.webgame.modules.match;

import com.webgame.common.BusinessException;
import com.webgame.modules.match.MatchModels.MatchResult;
import com.webgame.modules.match.MatchModels.PlayerActionCommand;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class MatchServiceImpl implements MatchService {

    private final AtomicLong matchIdSequence = new AtomicLong(1);
    private final Map<Long, Object> snapshots = new ConcurrentHashMap<>();

    @Override
    public Long createMatchFromRoom(Long roomId) {
        long matchId = matchIdSequence.getAndIncrement();
        snapshots.put(matchId, Map.of("roomId", roomId, "status", "created"));
        return matchId;
    }

    @Override
    public void startMatch(Long matchId) {
        snapshots.put(matchId, Map.of("matchId", matchId, "status", "started"));
    }

    @Override
    public void finishMatch(Long matchId, MatchResult result) {
        snapshots.put(matchId, Map.of("matchId", matchId, "status", "finished", "result", result));
    }

    @Override
    public void abortMatch(Long matchId) {
        snapshots.put(matchId, Map.of("matchId", matchId, "status", "aborted"));
    }

    @Override
    public void submitPlayerAction(PlayerActionCommand command) {
        if (command == null) {
            throw new BusinessException("MATCH_ACTION_INVALID", "player action is required");
        }
    }

    @Override
    public Object getMatchSnapshot(Long matchId) {
        return snapshots.getOrDefault(matchId, Map.of("matchId", matchId, "status", "missing"));
    }
}
