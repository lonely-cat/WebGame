package com.webgame.modules.asset;

import com.webgame.common.ApiResponse;
import com.webgame.common.UserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping("/me")
    public ApiResponse<AssetModels.UserAssetEntity> myAsset() {
        return ApiResponse.success(assetService.getUserAsset(UserContext.getCurrentUserId()));
    }

    @GetMapping("/me/logs")
    public ApiResponse<?> myAssetLogs() {
        return ApiResponse.success(assetService.listAssetLogs(UserContext.getCurrentUserId()));
    }
}
