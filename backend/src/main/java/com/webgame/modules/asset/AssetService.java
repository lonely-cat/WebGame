package com.webgame.modules.asset;

import com.webgame.modules.asset.AssetModels.AssetChangeRequest;
import com.webgame.modules.asset.AssetModels.AssetLogEntity;
import com.webgame.modules.asset.AssetModels.UserAssetEntity;
import java.util.List;

public interface AssetService {
    UserAssetEntity getUserAsset(Long userId);

    void addCoin(Long userId, Integer amount, String reason);

    void deductCoin(Long userId, Integer amount, String reason);

    void addScore(Long userId, Integer amount, String reason);

    void adjustAsset(AssetChangeRequest request);

    List<AssetLogEntity> listAssetLogs(Long userId);
}
