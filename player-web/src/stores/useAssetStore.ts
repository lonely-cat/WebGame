import { defineStore } from "pinia";
import { assetApi } from "../api/assetApi";

export const useAssetStore = defineStore("asset", {
  state: () => ({
    coin: 0,
    score: 0
  }),
  actions: {
    async fetchMyAsset() {
      const result = await assetApi.getMyAsset();
      const data = (result as any).data ?? {};
      this.coin = Number(data.coin ?? 0);
      this.score = Number(data.score ?? 0);
      return result;
    },
    async refreshAsset() {
      return this.fetchMyAsset();
    }
  }
});
