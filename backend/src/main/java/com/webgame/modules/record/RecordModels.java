package com.webgame.modules.record;

import com.webgame.common.BaseEntity;

public final class RecordModels {

    private RecordModels() {
    }

    public static class GameRecordEntity extends BaseEntity {
        public String gameCode;
        public String matchCode;
        public Long userId;
        public String result;
        public Integer score;
        public Integer coinReward;
        public Integer durationSeconds;
    }
}
