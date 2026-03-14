import { defineComponent, h } from "vue";
import type { RouteRecordRaw } from "vue-router";
import type { GameMeta } from "./types";

export class GameRegistry {
  private readonly games = new Map<string, GameMeta>();

  register(gameMeta: GameMeta) {
    this.games.set(gameMeta.gameCode, gameMeta);
  }

  getGame(gameCode: string) {
    return this.games.get(gameCode);
  }

  getAllGames() {
    return Array.from(this.games.values());
  }
}

export function createGameRoute(gameMeta: GameMeta): RouteRecordRaw {
  return {
    path: gameMeta.routePath,
    component: defineComponent({
      name: `${gameMeta.gameCode}-route`,
      setup() {
        return () => h("div", `${gameMeta.gameName} page`);
      }
    })
  };
}
