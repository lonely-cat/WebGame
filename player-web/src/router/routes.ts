import type { RouteRecordRaw } from "vue-router";
import { createGameRoute } from "../sdk/GameRegistry";
import { gameRegistry } from "../sdk/registry";
import ChineseChessShell from "../games/chinese-chess/ChineseChessShell.vue";
import ChineseChessRoomView from "../games/chinese-chess/ChineseChessRoomView.vue";
import ChineseChessMatchView from "../games/chinese-chess/ChineseChessMatchView.vue";
import DrawGuessShell from "../games/draw-guess/DrawGuessShell.vue";
import DrawGuessRoomView from "../games/draw-guess/DrawGuessRoomView.vue";
import DrawGuessMatchView from "../games/draw-guess/DrawGuessMatchView.vue";
import GomokuShell from "../games/gomoku/GomokuShell.vue";
import GomokuRoomView from "../games/gomoku/GomokuRoomView.vue";
import GomokuMatchView from "../games/gomoku/GomokuMatchView.vue";
import HomeView from "../views/HomeView.vue";
import LobbyView from "../views/LobbyView.vue";

export const routes: RouteRecordRaw[] = [
  { path: "/", component: HomeView },
  { path: "/lobby", component: LobbyView },
  {
    path: "/games/gomoku",
    component: GomokuShell,
    children: [
      { path: "", redirect: "/games/gomoku/room" },
      { path: "room", component: GomokuRoomView },
      { path: "match", component: GomokuMatchView }
    ]
  },
  {
    path: "/games/chinese-chess",
    component: ChineseChessShell,
    children: [
      { path: "", redirect: "/games/chinese-chess/room" },
      { path: "room", component: ChineseChessRoomView },
      { path: "match", component: ChineseChessMatchView }
    ]
  },
  {
    path: "/games/draw-guess",
    component: DrawGuessShell,
    children: [
      { path: "", redirect: "/games/draw-guess/room" },
      { path: "room", component: DrawGuessRoomView },
      { path: "match", component: DrawGuessMatchView }
    ]
  },
  ...gameRegistry.getAllGames().filter((game) => !["gomoku", "chinese-chess", "draw-guess"].includes(game.gameCode)).map(createGameRoute)
];
