package com.webgame.common.repository;

import com.webgame.modules.game.GameModels.GameConfigEntity;
import com.webgame.modules.game.GameModels.GameEntity;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class GameRepository {

    private static final RowMapper<GameEntity> GAME_ROW_MAPPER = GameRepository::mapGame;
    private static final RowMapper<GameConfigEntity> GAME_CONFIG_ROW_MAPPER = GameRepository::mapGameConfig;

    private final JdbcTemplate jdbcTemplate;

    public GameRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<GameEntity> findOnlineGames() {
        return jdbcTemplate.query("SELECT * FROM game WHERE status = 1 ORDER BY sort_no ASC, id ASC", GAME_ROW_MAPPER);
    }

    public Optional<GameEntity> findByCode(String gameCode) {
        List<GameEntity> games = jdbcTemplate.query("SELECT * FROM game WHERE game_code = ?", GAME_ROW_MAPPER, gameCode);
        return games.stream().findFirst();
    }

    public List<GameConfigEntity> findConfigsByGameCode(String gameCode) {
        return jdbcTemplate.query(
                "SELECT * FROM game_config WHERE game_code = ? ORDER BY id ASC",
                GAME_CONFIG_ROW_MAPPER,
                gameCode
        );
    }

    public void insert(GameEntity game, int sortNo) {
        jdbcTemplate.update(
                """
                INSERT INTO game (game_code, game_name, game_type, icon, status, supports_multiplayer, route_path, sort_no, create_time, update_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                game.gameCode,
                game.gameName,
                game.gameType,
                game.icon,
                game.status,
                Boolean.TRUE.equals(game.supportsMultiplayer) ? 1 : 0,
                game.routePath,
                sortNo,
                game.getCreateTime(),
                game.getUpdateTime()
        );
    }

    public void updateStatus(String gameCode, Integer status, LocalDateTime updateTime) {
        jdbcTemplate.update("UPDATE game SET status = ?, update_time = ? WHERE game_code = ?", status, updateTime, gameCode);
    }

    public void saveConfig(GameConfigEntity config) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM game_config WHERE game_code = ? AND config_key = ?",
                Integer.class,
                config.gameCode,
                config.configKey
        );
        if (count != null && count > 0) {
            jdbcTemplate.update(
                    "UPDATE game_config SET config_value = ?, update_time = ? WHERE game_code = ? AND config_key = ?",
                    config.configValue,
                    config.getUpdateTime(),
                    config.gameCode,
                    config.configKey
            );
        } else {
            jdbcTemplate.update(
                    """
                    INSERT INTO game_config (game_code, config_key, config_value, create_time, update_time)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    config.gameCode,
                    config.configKey,
                    config.configValue,
                    config.getCreateTime(),
                    config.getUpdateTime()
            );
        }
    }

    public boolean hasAnyGames() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM game", Integer.class);
        return count != null && count > 0;
    }

    private static GameEntity mapGame(ResultSet rs, int rowNum) throws SQLException {
        GameEntity game = new GameEntity();
        game.setId(rs.getLong("id"));
        game.gameCode = rs.getString("game_code");
        game.gameName = rs.getString("game_name");
        game.gameType = rs.getString("game_type");
        game.icon = rs.getString("icon");
        game.status = rs.getInt("status");
        game.supportsMultiplayer = rs.getInt("supports_multiplayer") == 1;
        game.routePath = rs.getString("route_path");
        game.setCreateTime(toDateTime(rs, "create_time"));
        game.setUpdateTime(toDateTime(rs, "update_time"));
        game.setDeleted(false);
        return game;
    }

    private static GameConfigEntity mapGameConfig(ResultSet rs, int rowNum) throws SQLException {
        GameConfigEntity config = new GameConfigEntity();
        config.setId(rs.getLong("id"));
        config.gameCode = rs.getString("game_code");
        config.configKey = rs.getString("config_key");
        config.configValue = rs.getString("config_value");
        config.setCreateTime(toDateTime(rs, "create_time"));
        config.setUpdateTime(toDateTime(rs, "update_time"));
        config.setDeleted(false);
        return config;
    }

    private static LocalDateTime toDateTime(ResultSet rs, String column) throws SQLException {
        return rs.getTimestamp(column) == null ? null : rs.getTimestamp(column).toLocalDateTime();
    }
}
