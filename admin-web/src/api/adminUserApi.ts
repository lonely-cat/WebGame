export const adminUserApi = {
  pageUsers: () => fetch("/api/admin/users"),
  getUserDetail: (userId: number) => fetch(`/api/admin/users/${userId}`),
  banUser: (userId: number) => fetch(`/api/admin/users/${userId}/ban`, { method: "POST" }),
  unbanUser: (userId: number) => fetch(`/api/admin/users/${userId}/unban`, { method: "POST" })
};
