# Database Design

## 1. Design Goals

- support multiple games under one platform
- separate platform data from game runtime data
- keep user assets and asset logs auditable
- support multiplayer room and match lifecycle
- support rankings and admin management

## 2. Core Tables

### `user`

Purpose:

- store player account and profile data

Fields:

- `id` bigint primary key
- `username` varchar(64) unique not null
- `nickname` varchar(64) not null
- `password` varchar(255) not null
- `avatar` varchar(255) null
- `status` tinyint not null default 1
- `last_login_time` datetime null
- `create_time` datetime not null
- `update_time` datetime not null
- `deleted` tinyint not null default 0

### `role`

Purpose:

- store platform roles

Fields:

- `id` bigint primary key
- `role_code` varchar(64) unique not null
- `role_name` varchar(64) not null

### `user_role`

Purpose:

- user-role relation

Fields:

- `id` bigint primary key
- `user_id` bigint not null
- `role_id` bigint not null

### `user_asset`

Purpose:

- store player platform-wide assets

Fields:

- `id` bigint primary key
- `user_id` bigint unique not null
- `score` int not null default 0
- `coin` int not null default 0
- `level` int not null default 1
- `exp` int not null default 0
- `create_time` datetime not null
- `update_time` datetime not null

### `asset_log`

Purpose:

- store asset changes for audit and admin review

Fields:

- `id` bigint primary key
- `user_id` bigint not null
- `asset_type` varchar(32) not null
- `change_amount` int not null
- `before_value` int not null
- `after_value` int not null
- `reason` varchar(255) not null
- `game_code` varchar(64) null
- `biz_id` varchar(64) null
- `create_time` datetime not null

### `game`

Purpose:

- store game definitions exposed in the lobby

Fields:

- `id` bigint primary key
- `game_code` varchar(64) unique not null
- `game_name` varchar(64) not null
- `game_type` varchar(64) not null
- `icon` varchar(255) null
- `status` tinyint not null default 1
- `supports_multiplayer` tinyint not null default 1
- `route_path` varchar(128) not null
- `sort_no` int not null default 0
- `create_time` datetime not null
- `update_time` datetime not null

### `game_config`

Purpose:

- store configurable per-game settings

Fields:

- `id` bigint primary key
- `game_code` varchar(64) not null
- `config_key` varchar(64) not null
- `config_value` text not null
- `create_time` datetime not null
- `update_time` datetime not null

### `game_room`

Purpose:

- store multiplayer room state

Fields:

- `id` bigint primary key
- `room_code` varchar(32) unique not null
- `game_code` varchar(64) not null
- `room_status` tinyint not null
- `owner_user_id` bigint not null
- `max_players` int not null
- `current_players` int not null default 0
- `create_time` datetime not null
- `update_time` datetime not null

### `room_player`

Purpose:

- store users inside rooms

Fields:

- `id` bigint primary key
- `room_id` bigint not null
- `user_id` bigint not null
- `seat_no` int not null
- `ready_status` tinyint not null default 0
- `online_status` tinyint not null default 1
- `create_time` datetime not null
- `update_time` datetime not null

### `game_match`

Purpose:

- store an active or completed match session

Fields:

- `id` bigint primary key
- `match_code` varchar(32) unique not null
- `game_code` varchar(64) not null
- `room_id` bigint null
- `status` tinyint not null
- `start_time` datetime null
- `end_time` datetime null
- `winner_user_id` bigint null
- `create_time` datetime not null
- `update_time` datetime not null

### `match_player`

Purpose:

- store players in a match

Fields:

- `id` bigint primary key
- `match_id` bigint not null
- `user_id` bigint not null
- `seat_no` int not null
- `result` varchar(32) null
- `score` int not null default 0
- `create_time` datetime not null
- `update_time` datetime not null

### `game_record`

Purpose:

- store per-user match result records

Fields:

- `id` bigint primary key
- `game_code` varchar(64) not null
- `match_code` varchar(32) not null
- `user_id` bigint not null
- `result` varchar(32) not null
- `score` int not null default 0
- `coin_reward` int not null default 0
- `duration_seconds` int not null default 0
- `create_time` datetime not null

### `game_rank`

Purpose:

- cache ranking snapshots

Fields:

- `id` bigint primary key
- `game_code` varchar(64) not null
- `user_id` bigint not null
- `rank_type` varchar(32) not null
- `rank_score` int not null
- `rank_position` int not null
- `refresh_time` datetime not null

### `notice`

Purpose:

- store system notices

Fields:

- `id` bigint primary key
- `title` varchar(128) not null
- `content` text not null
- `status` tinyint not null default 1
- `create_time` datetime not null
- `update_time` datetime not null

### `admin_log`

Purpose:

- store admin operations for traceability

Fields:

- `id` bigint primary key
- `admin_user_id` bigint not null
- `action` varchar(64) not null
- `target_type` varchar(64) not null
- `target_id` varchar(64) not null
- `detail` text null
- `create_time` datetime not null

## 3. Seed Data

Recommended seed content:

- default roles: `ROLE_ADMIN`, `ROLE_USER`
- initial admin account
- initial games: Gomoku, Chinese Chess, Blackjack, Doodle, Draw and Guess
- initial game configs for room limits and reward rules

## 4. Current Implementation Note

The current backend still uses in-memory placeholders for core services.
This design document and the SQL draft should be treated as the persistence target for the next implementation step.
