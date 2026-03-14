import { http } from "../utils/http";

export const recordApi = {
  getMyRecords: (gameCode?: string) => http(`/records/me${gameCode ? `?gameCode=${gameCode}` : ""}`),
  getRank: (gameCode: string) => http(`/ranks/global?gameCode=${gameCode}`)
};
