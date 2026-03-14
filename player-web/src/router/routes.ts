import type { RouteRecordRaw } from "vue-router";
import { createGameRoute } from "../sdk/GameRegistry";
import { gameRegistry } from "../sdk/registry";
import HomeView from "../views/HomeView.vue";
import LobbyView from "../views/LobbyView.vue";

export const routes: RouteRecordRaw[] = [
  { path: "/", component: HomeView },
  { path: "/lobby", component: LobbyView },
  ...gameRegistry.getAllGames().map(createGameRoute)
];
