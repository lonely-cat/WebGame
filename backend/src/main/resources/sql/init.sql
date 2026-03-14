CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    nickname VARCHAR(64) NOT NULL,
    password VARCHAR(255) NOT NULL,
    avatar VARCHAR(255) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    last_login_time DATETIME NULL,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL UNIQUE,
    role_name VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS user_asset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    score INT NOT NULL DEFAULT 0,
    coin INT NOT NULL DEFAULT 0,
    level INT NOT NULL DEFAULT 1,
    exp INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS asset_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    asset_type VARCHAR(32) NOT NULL,
    change_amount INT NOT NULL,
    before_value INT NOT NULL,
    after_value INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    game_code VARCHAR(64) NULL,
    biz_id VARCHAR(64) NULL,
    create_time DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS game (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    game_code VARCHAR(64) NOT NULL UNIQUE,
    game_name VARCHAR(64) NOT NULL,
    game_type VARCHAR(64) NOT NULL,
    icon VARCHAR(255) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    supports_multiplayer TINYINT NOT NULL DEFAULT 1,
    route_path VARCHAR(128) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS game_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    game_code VARCHAR(64) NOT NULL,
    config_key VARCHAR(64) NOT NULL,
    config_value TEXT NOT NULL,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS game_room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_code VARCHAR(32) NOT NULL UNIQUE,
    game_code VARCHAR(64) NOT NULL,
    room_status TINYINT NOT NULL,
    owner_user_id BIGINT NOT NULL,
    max_players INT NOT NULL,
    current_players INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS room_player (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    seat_no INT NOT NULL,
    ready_status TINYINT NOT NULL DEFAULT 0,
    online_status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS game_match (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    match_code VARCHAR(32) NOT NULL UNIQUE,
    game_code VARCHAR(64) NOT NULL,
    room_id BIGINT NULL,
    status TINYINT NOT NULL,
    start_time DATETIME NULL,
    end_time DATETIME NULL,
    winner_user_id BIGINT NULL,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS match_player (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    match_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    seat_no INT NOT NULL,
    result VARCHAR(32) NULL,
    score INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS game_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    game_code VARCHAR(64) NOT NULL,
    match_code VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    result VARCHAR(32) NOT NULL,
    score INT NOT NULL DEFAULT 0,
    coin_reward INT NOT NULL DEFAULT 0,
    duration_seconds INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS game_rank (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    game_code VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    rank_type VARCHAR(32) NOT NULL,
    rank_score INT NOT NULL,
    rank_position INT NOT NULL,
    refresh_time DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(128) NOT NULL,
    content TEXT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS admin_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_user_id BIGINT NOT NULL,
    action VARCHAR(64) NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    target_id VARCHAR(64) NOT NULL,
    detail TEXT NULL,
    create_time DATETIME NOT NULL
);

INSERT INTO role (role_code, role_name)
SELECT 'ROLE_ADMIN', 'Administrator'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE role_code = 'ROLE_ADMIN');

INSERT INTO role (role_code, role_name)
SELECT 'ROLE_USER', 'Player'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE role_code = 'ROLE_USER');
