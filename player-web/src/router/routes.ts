import type { RouteRecordRaw } from "vue-router";
import { createGameRoute } from "../sdk/GameRegistry";
import { gameRegistry } from "../sdk/registry";
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
  ...gameRegistry.getAllGames().filter((game) => game.gameCode !== "gomoku").map(createGameRoute)
];
