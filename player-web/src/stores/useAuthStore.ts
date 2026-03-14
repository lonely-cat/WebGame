import { defineStore } from "pinia";
import { authApi } from "../api/authApi";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: "",
    userInfo: null as null | Record<string, unknown>
  }),
  actions: {
    async login(username: string, password: string) {
      const result = await authApi.login({ username, password });
      this.token = String((result as any).data?.accessToken ?? "");
      if (this.token) {
        localStorage.setItem("webgame_token", this.token);
      }
      return result;
    },
    logout() {
      this.token = "";
      this.userInfo = null;
      localStorage.removeItem("webgame_token");
    },
    async fetchProfile() {
      const result = await authApi.getProfile();
      this.userInfo = (result as any).data ?? null;
      return result;
    }
  }
});
