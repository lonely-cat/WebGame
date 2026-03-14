export const adminDashboardApi = {
  getOverview: () => fetch("/api/admin/dashboard/overview"),
  getGameStats: () => fetch("/api/admin/dashboard/games"),
  getOnlineStats: () => fetch("/api/admin/dashboard/online")
};
