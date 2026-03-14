import { http } from "../utils/http";

export const gameApi = {
  getGameList: () => http("/games"),
  getGameDetail: (gameCode: string) => http(`/games/${gameCode}`),
  getGameConfig: (gameCode: string) => http(`/games/${gameCode}/configs`)
};
