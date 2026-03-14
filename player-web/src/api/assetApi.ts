import { http } from "../utils/http";

export const assetApi = {
  getMyAsset: () => http("/assets/me"),
  getAssetLogs: () => http("/assets/me/logs")
};
