package com.webgame.common.repository;

import com.webgame.modules.asset.AssetModels.AssetLogEntity;
import com.webgame.modules.asset.AssetModels.UserAssetEntity;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AssetRepository {

    private static final RowMapper<UserAssetEntity> ASSET_ROW_MAPPER = AssetRepository::mapAsset;
    private static final RowMapper<AssetLogEntity> ASSET_LOG_ROW_MAPPER = AssetRepository::mapAssetLog;

    private final JdbcTemplate jdbcTemplate;

    public AssetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserAssetEntity> findByUserId(Long userId) {
        List<UserAssetEntity> assets = jdbcTemplate.query("SELECT * FROM user_asset WHERE user_id = ?", ASSET_ROW_MAPPER, userId);
        return assets.stream().findFirst();
    }

    public void insert(UserAssetEntity asset) {
        jdbcTemplate.update(
                """
                INSERT INTO user_asset (user_id, score, coin, level, exp, create_time, update_time)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                asset.userId,
                asset.score,
                asset.coin,
                asset.level,
                asset.exp,
                asset.getCreateTime(),
                asset.getUpdateTime()
        );
    }

    public void update(UserAssetEntity asset) {
        jdbcTemplate.update(
                """
                UPDATE user_asset
                SET score = ?, coin = ?, level = ?, exp = ?, update_time = ?
                WHERE user_id = ?
                """,
                asset.score,
                asset.coin,
                asset.level,
                asset.exp,
                asset.getUpdateTime(),
                asset.userId
        );
    }

    public void insertLog(AssetLogEntity log) {
        jdbcTemplate.update(
                """
                INSERT INTO asset_log (user_id, asset_type, change_amount, before_value, after_value, reason, game_code, biz_id, create_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                log.userId,
                log.assetType,
                log.changeAmount,
                log.beforeValue,
                log.afterValue,
                log.reason,
                log.gameCode,
                log.bizId,
                log.getCreateTime()
        );
    }

    public List<AssetLogEntity> findLogsByUserId(Long userId) {
        return jdbcTemplate.query(
                "SELECT * FROM asset_log WHERE user_id = ? ORDER BY id DESC",
                ASSET_LOG_ROW_MAPPER,
                userId
        );
    }

    private static UserAssetEntity mapAsset(ResultSet rs, int rowNum) throws SQLException {
        UserAssetEntity asset = new UserAssetEntity();
        asset.setId(rs.getLong("id"));
        asset.userId = rs.getLong("user_id");
        asset.score = rs.getInt("score");
        asset.coin = rs.getInt("coin");
        asset.level = rs.getInt("level");
        asset.exp = rs.getInt("exp");
        asset.setCreateTime(toDateTime(rs, "create_time"));
        asset.setUpdateTime(toDateTime(rs, "update_time"));
        asset.setDeleted(false);
        return asset;
    }

    private static AssetLogEntity mapAssetLog(ResultSet rs, int rowNum) throws SQLException {
        AssetLogEntity log = new AssetLogEntity();
        log.setId(rs.getLong("id"));
        log.userId = rs.getLong("user_id");
        log.assetType = rs.getString("asset_type");
        log.changeAmount = rs.getInt("change_amount");
        log.beforeValue = rs.getInt("before_value");
        log.afterValue = rs.getInt("after_value");
        log.reason = rs.getString("reason");
        log.gameCode = rs.getString("game_code");
        log.bizId = rs.getString("biz_id");
        log.setCreateTime(toDateTime(rs, "create_time"));
        log.setUpdateTime(log.getCreateTime());
        log.setDeleted(false);
        return log;
    }

    private static LocalDateTime toDateTime(ResultSet rs, String column) throws SQLException {
        return rs.getTimestamp(column) == null ? null : rs.getTimestamp(column).toLocalDateTime();
    }
}
