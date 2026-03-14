package com.webgame.modules.rank;

public interface RankService {
    Object getGlobalRank(String gameCode);

    Object getWeeklyRank(String gameCode);

    void refreshRank(String gameCode);

    Object getUserRank(Long userId, String gameCode);
}
