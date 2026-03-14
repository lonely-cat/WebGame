import { defineStore } from "pinia";
import { GameSocketClient } from "../websocket/GameSocketClient";

export const useWsStore = defineStore("ws", {
  state: () => ({
    connected: false,
    lastMessage: null as unknown,
    client: new GameSocketClient()
  }),
  actions: {
    connect(token: string, userId: number) {
      this.client.connect(token, userId);
      this.connected = true;
    },
    disconnect() {
      this.client.disconnect();
      this.connected = false;
    },
    sendMessage(message: unknown) {
      this.client.send(message);
    }
  }
});
