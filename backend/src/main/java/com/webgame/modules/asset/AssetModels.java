package com.webgame.modules.asset;

import com.webgame.common.BaseEntity;

public final class AssetModels {

    private AssetModels() {
    }

    public static class UserAssetEntity extends BaseEntity {
        public Long userId;
        public Integer score;
        public Integer coin;
        public Integer level;
        public Integer exp;
    }

    public static class AssetLogEntity extends BaseEntity {
        public Long userId;
        public String assetType;
        public Integer changeAmount;
        public Integer beforeValue;
        public Integer afterValue;
        public String reason;
        public String gameCode;
        public String bizId;
    }

    public record AssetChangeRequest(Long userId, String assetType, Integer changeAmount, String reason) {
    }
}
