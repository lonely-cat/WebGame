export const adminAssetApi = {
  pageUserAssets: () => fetch("/api/admin/assets"),
  adjustCoin: (data: unknown) =>
    fetch("/api/admin/assets/coin", { method: "POST", body: JSON.stringify(data) }),
  adjustScore: (data: unknown) =>
    fetch("/api/admin/assets/score", { method: "POST", body: JSON.stringify(data) }),
  getAssetLogs: (userId: number) => fetch(`/api/admin/users/${userId}`)
};
