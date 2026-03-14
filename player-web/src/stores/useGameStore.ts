import { defineStore } from "pinia";
import { gameApi } from "../api/gameApi";

export const useGameStore = defineStore("game", {
  state: () => ({
    gameList: [] as unknown[],
    currentGame: null as unknown,
    gameConfig: [] as unknown[]
  }),
  actions: {
    async loadGames() {
      const result = await gameApi.getGameList();
      this.gameList = (result as any).data ?? [];
      return result;
    },
    async loadGameDetail(gameCode: string) {
      const result = await gameApi.getGameDetail(gameCode);
      this.currentGame = (result as any).data ?? null;
      return result;
    },
    async loadGameConfig(gameCode: string) {
      const result = await gameApi.getGameConfig(gameCode);
      this.gameConfig = (result as any).data ?? [];
      return result;
    }
  }
});
