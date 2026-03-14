package com.webgame.modules.record;

import com.webgame.modules.match.MatchModels.MatchResult;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RecordServiceImpl implements RecordService {

    @Override
    public void saveMatchRecords(MatchResult result) {
    }

    @Override
    public Object listUserRecords(Long userId, String gameCode) {
        return List.of(Map.of("userId", userId, "gameCode", gameCode, "status", "todo"));
    }

    @Override
    public Object listGameRecords(String gameCode) {
        return List.of(Map.of("gameCode", gameCode, "status", "todo"));
    }
}
