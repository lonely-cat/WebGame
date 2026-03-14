package com.webgame.common.repository;

import com.webgame.modules.user.UserEntity;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private static final RowMapper<UserEntity> USER_ROW_MAPPER = UserRepository::mapUser;

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserEntity> findById(Long userId) {
        List<UserEntity> users = jdbcTemplate.query("SELECT * FROM user WHERE id = ? AND deleted = 0", USER_ROW_MAPPER, userId);
        return users.stream().findFirst();
    }

    public Optional<UserEntity> findByUsername(String username) {
        List<UserEntity> users = jdbcTemplate.query(
                "SELECT * FROM user WHERE username = ? AND deleted = 0",
                USER_ROW_MAPPER,
                username
        );
        return users.stream().findFirst();
    }

    public long insert(UserEntity user) {
        jdbcTemplate.update(
                """
                INSERT INTO user (username, nickname, password, avatar, status, last_login_time, create_time, update_time, deleted)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                user.getUsername(),
                user.getNickname(),
                user.getPassword(),
                user.getAvatar(),
                user.getStatus(),
                user.getLastLoginTime(),
                user.getCreateTime(),
                user.getUpdateTime(),
                Boolean.TRUE.equals(user.getDeleted()) ? 1 : 0
        );
        Long id = jdbcTemplate.queryForObject("SELECT id FROM user WHERE username = ?", Long.class, user.getUsername());
        return id == null ? 0L : id;
    }

    public void update(UserEntity user) {
        jdbcTemplate.update(
                """
                UPDATE user
                SET nickname = ?, avatar = ?, password = ?, status = ?, last_login_time = ?, update_time = ?
                WHERE id = ?
                """,
                user.getNickname(),
                user.getAvatar(),
                user.getPassword(),
                user.getStatus(),
                user.getLastLoginTime(),
                user.getUpdateTime(),
                user.getId()
        );
    }

    public boolean existsAdminUser() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM user WHERE username = 'admin' AND deleted = 0", Integer.class);
        return count != null && count > 0;
    }

    private static UserEntity mapUser(ResultSet rs, int rowNum) throws SQLException {
        UserEntity user = new UserEntity();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setNickname(rs.getString("nickname"));
        user.setPassword(rs.getString("password"));
        user.setAvatar(rs.getString("avatar"));
        user.setStatus(rs.getInt("status"));
        user.setLastLoginTime(rs.getTimestamp("last_login_time") == null ? null : rs.getTimestamp("last_login_time").toLocalDateTime());
        user.setCreateTime(toDateTime(rs, "create_time"));
        user.setUpdateTime(toDateTime(rs, "update_time"));
        user.setDeleted(rs.getInt("deleted") == 1);
        return user;
    }

    private static LocalDateTime toDateTime(ResultSet rs, String column) throws SQLException {
        return rs.getTimestamp(column) == null ? null : rs.getTimestamp(column).toLocalDateTime();
    }
}
