import { BaseGame } from "./BaseGame";
import type { GameSocketClient } from "../websocket/GameSocketClient";

export abstract class BaseMultiplayerGame extends BaseGame {
  protected socketClient: GameSocketClient | null = null;

  bindSocket(socketClient: GameSocketClient) {
    this.socketClient = socketClient;
  }

  sendAction(action: unknown) {
    this.socketClient?.send({ type: "PLAYER_ACTION", payload: action });
  }

  onPlayerJoined(_player: unknown) {}

  onPlayerLeft(_player: unknown) {}

  onStateSync(_state: unknown) {}

  onMatchEnd(_result: unknown) {}
}
