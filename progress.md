Original prompt: 这个项目我想创建一个一些小游戏的一个集合网站,比如说像象棋、五子棋和一些扑克游戏之类的,还有像那种涂鸦之类的。涂鸦小游戏。然后你帮我先列一个计划出来吧,后台的话就使用SpringBoot,认证的话还是用那个安全框架吧。前端的话可以使用Vue,游戏的部分的话,如果使用Vue比较难以创建的话,就使用普通的静态网页站吧,静态网页吧。

- Created planning docs under `docs/`.
- Built backend/player/admin skeletons and pushed initial project structure.
- Connected MySQL and Redis, created dedicated `webgame` database user, and documented infra rules in a local skill.
- Switched auth/user/asset/game flows to database-backed services.
- Added first room and websocket scaffolding plus bootstrap seed data.
- Fixed JWT secret length so login can issue tokens.
- Added basic service implementations for rank/record/match so the backend can boot.
- Verified backend startup and HTTP flow: login, list games, query assets, create room.
- Built the first playable Gomoku page with room controls, canvas rendering, `render_game_to_text`, and optimistic move placement.
- Verified Gomoku in Playwright twice: single-client room flow and dual-client websocket sync, with both clients reflecting the same move state.
- Moved Gomoku turn and move validation to the backend rule engine, and updated the frontend to wait for server-approved state sync before rendering moves.
- Replaced the placeholder player entry points with a proper landing page and lobby grid, so new games can be surfaced through a stable front-door instead of direct prototype links.
- Restructured the Gomoku page into a clearer room-to-match flow with explicit login, room staging, roster, live board, and feed sections while keeping existing multiplayer test selectors stable.
- Re-ran the frontend build plus the Playwright Gomoku checks after the UI refactor; dual-client move sync and illegal consecutive move rejection still pass.
- Split Gomoku into a dedicated shell with separate `/room` and `/match` routes, backed by a shared session composable so socket state and match state survive route changes.
- Verified the new route flow with build output, Playwright screenshots, dual-client sync, and the illegal-move rejection script.
- Added a shared frontend websocket protocol module for room, match-start, state-sync, action, and error payloads so game sessions can stop hand-rolling message parsing.
- Refactored the Gomoku session composable to consume those shared protocol types and helpers, then re-verified the room flow and multiplayer sync.
- Extracted reusable multiplayer shell and two-column layout components, then rewired Gomoku to use them so future games can inherit the same room and match framing.
- Rebuilt and reran the Gomoku multiplayer regression after the shared UI extraction; screenshots and dual-client sync remain healthy.
- Added a reusable `useMultiplayerRoomSession` composable for platform-level login, room creation, socket connection, ready flow, match-start flow, and text-state output.
- Used that shared room session to bring Chinese Chess onto the same multiplayer shell, with working `/room` and `/match` routes and a validated dual-client room-to-match transition.
- Fixed a real shared-state bug while doing this: room shells and child routes now reuse one cached session per game code instead of accidentally creating separate websocket/session state.
- Extended the shared room session with game-specific websocket hooks and generic client-message sending so platform state can be reused without blocking game-specific action handling.
- Moved Gomoku onto that shared room session path for login/build room/join/start plumbing while keeping move application, board rendering, and server-state sync inside the Gomoku layer.
- Re-ran Gomoku multiplayer sync, Gomoku illegal-move rejection, and the Chinese Chess room-to-match check after the refactor; all three still pass.

TODO:
- Promote the current room websocket payloads into a stable protocol shared by frontend and backend.
- Continue shrinking the remaining Gomoku-specific session code so only pure board/rule behavior stays in the game folder.
- Plug Draw and Guess into the shared multiplayer shell after the generic room flow is stable.
- Add a cleaner persistent dev-start workflow for backend and frontend.
