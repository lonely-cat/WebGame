import { defineStore } from "pinia";
import { roomApi } from "../api/roomApi";

export const useRoomStore = defineStore("room", {
  state: () => ({
    currentRoom: null as unknown,
    roomPlayers: [] as unknown[],
    readyMap: {} as Record<string, boolean>
  }),
  actions: {
    async createRoom(gameCode: string, maxPlayers: number) {
      const result = await roomApi.createRoom({ gameCode, maxPlayers });
      this.currentRoom = (result as any).data ?? null;
      return result;
    },
    async joinRoom(roomCode: string) {
      const result = await roomApi.joinRoom(roomCode);
      this.currentRoom = (result as any).data ?? null;
      return result;
    },
    async leaveRoom(roomId: number) {
      return roomApi.leaveRoom(roomId);
    },
    async setReady(roomId: number, ready: boolean) {
      return ready ? roomApi.ready(roomId) : roomApi.cancelReady(roomId);
    }
  }
});
