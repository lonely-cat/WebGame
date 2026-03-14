import { http } from "../utils/http";

export const authApi = {
  login: (data: { username: string; password: string }) =>
    http("/auth/login", { method: "POST", body: JSON.stringify(data) }),
  register: (data: { username: string; password: string; nickname: string }) =>
    http("/auth/register", { method: "POST", body: JSON.stringify(data) }),
  refreshToken: (refreshToken: string) =>
    http("/auth/refresh-token", { method: "POST", body: JSON.stringify({ refreshToken }) }),
  getProfile: () => http("/auth/profile")
};
