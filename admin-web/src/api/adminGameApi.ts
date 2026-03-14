export const adminGameApi = {
  pageGames: () => fetch("/api/admin/games"),
  saveGame: (data: unknown) => fetch("/api/admin/games", { method: "POST", body: JSON.stringify(data) }),
  updateGame: (data: unknown) =>
    fetch("/api/admin/games/update", { method: "POST", body: JSON.stringify(data) }),
  updateGameConfig: (data: unknown) =>
    fetch("/api/admin/games/update", { method: "POST", body: JSON.stringify(data) })
};
