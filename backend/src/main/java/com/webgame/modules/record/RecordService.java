package com.webgame.modules.record;

import com.webgame.modules.match.MatchModels.MatchResult;

public interface RecordService {
    void saveMatchRecords(MatchResult result);

    Object listUserRecords(Long userId, String gameCode);

    Object listGameRecords(String gameCode);
}
