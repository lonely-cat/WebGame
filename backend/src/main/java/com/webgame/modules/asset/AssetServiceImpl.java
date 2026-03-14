package com.webgame.modules.asset;

import com.webgame.common.BusinessException;
import com.webgame.common.repository.AssetRepository;
import com.webgame.modules.asset.AssetModels.AssetChangeRequest;
import com.webgame.modules.asset.AssetModels.AssetLogEntity;
import com.webgame.modules.asset.AssetModels.UserAssetEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;

    public AssetServiceImpl(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public UserAssetEntity getUserAsset(Long userId) {
        return assetRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("ASSET_NOT_FOUND", "asset not found"));
    }

    @Override
    public void addCoin(Long userId, Integer amount, String reason) {
        changeAsset(userId, "coin", amount, reason);
    }

    @Override
    public void deductCoin(Long userId, Integer amount, String reason) {
        changeAsset(userId, "coin", -Math.abs(amount), reason);
    }

    @Override
    public void addScore(Long userId, Integer amount, String reason) {
        changeAsset(userId, "score", amount, reason);
    }

    @Override
    public void adjustAsset(AssetChangeRequest request) {
        changeAsset(request.userId(), request.assetType(), request.changeAmount(), request.reason());
    }

    @Override
    public List<AssetLogEntity> listAssetLogs(Long userId) {
        return assetRepository.findLogsByUserId(userId);
    }

    private void changeAsset(Long userId, String assetType, Integer changeAmount, String reason) {
        UserAssetEntity asset = getUserAsset(userId);
        int before;
        int after;
        if ("coin".equalsIgnoreCase(assetType)) {
            before = asset.coin;
            after = before + changeAmount;
            if (after < 0) {
                throw new BusinessException("ASSET_NOT_ENOUGH", "coin is not enough");
            }
            asset.coin = after;
        } else if ("score".equalsIgnoreCase(assetType)) {
            before = asset.score;
            after = before + changeAmount;
            asset.score = Math.max(0, after);
            after = asset.score;
        } else {
            throw new BusinessException("ASSET_TYPE_UNSUPPORTED", "unsupported asset type");
        }

        asset.setUpdateTime(LocalDateTime.now());
        assetRepository.update(asset);
        assetRepository.insertLog(buildLog(userId, assetType, changeAmount, before, after, reason));
    }

    private AssetLogEntity buildLog(Long userId, String assetType, Integer changeAmount, int before, int after,
                                    String reason) {
        AssetLogEntity log = new AssetLogEntity();
        log.userId = userId;
        log.assetType = assetType;
        log.changeAmount = changeAmount;
        log.beforeValue = before;
        log.afterValue = after;
        log.reason = reason;
        log.gameCode = null;
        log.bizId = null;
        log.setCreateTime(LocalDateTime.now());
        log.setUpdateTime(LocalDateTime.now());
        log.setDeleted(false);
        return log;
    }
}
