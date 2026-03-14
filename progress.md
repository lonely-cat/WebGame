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

TODO:
- Add server-side Gomoku rule validation so move legality is enforced on the backend.
- Promote the current room websocket payloads into a stable protocol shared by frontend and backend.
- Add a cleaner persistent dev-start workflow for backend and frontend.
