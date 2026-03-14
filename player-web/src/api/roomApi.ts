import { http } from "../utils/http";

export const roomApi = {
  createRoom: (data: { gameCode: string; maxPlayers: number }) =>
    http("/rooms", { method: "POST", body: JSON.stringify(data) }),
  joinRoom: (roomCode: string) =>
    http("/rooms/join", { method: "POST", body: JSON.stringify({ roomCode }) }),
  leaveRoom: (roomId: number) =>
    http("/rooms/leave", { method: "POST", body: JSON.stringify({ roomId }) }),
  ready: (roomId: number) =>
    http("/rooms/ready", { method: "POST", body: JSON.stringify({ roomId }) }),
  cancelReady: (roomId: number) =>
    http("/rooms/cancel-ready", { method: "POST", body: JSON.stringify({ roomId }) }),
  getRoomDetail: (roomCode: string) => http(`/rooms/detail?roomCode=${roomCode}`)
};
