package com.webgame.modules.rank;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RankServiceImpl implements RankService {

    @Override
    public Object getGlobalRank(String gameCode) {
        return List.of(Map.of("gameCode", gameCode, "rankType", "global", "entries", List.of()));
    }

    @Override
    public Object getWeeklyRank(String gameCode) {
        return List.of(Map.of("gameCode", gameCode, "rankType", "weekly", "entries", List.of()));
    }

    @Override
    public void refreshRank(String gameCode) {
    }

    @Override
    public Object getUserRank(Long userId, String gameCode) {
        return Map.of("userId", userId, "gameCode", gameCode, "rank", 0, "score", 0);
    }
}
