import type { GameSocketClient } from "./GameSocketClient";

export class RoomChannel {
  constructor(private readonly socketClient: GameSocketClient) {}

  joinRoom(roomCode: string) {
    this.socketClient.send({ type: "ROOM_JOIN", roomCode });
  }

  leaveRoom(roomCode: string) {
    this.socketClient.send({ type: "ROOM_LEAVE", roomCode });
  }

  ready(roomCode: string) {
    this.socketClient.send({ type: "ROOM_READY", roomCode });
  }

  cancelReady(roomCode: string) {
    this.socketClient.send({ type: "ROOM_CANCEL_READY", roomCode });
  }
}
