import type { GameSocketClient } from "./GameSocketClient";

export class MatchChannel {
  constructor(private readonly socketClient: GameSocketClient) {}

  startMatch(matchCode: string) {
    this.socketClient.send({ type: "MATCH_START", matchCode });
  }

  sendPlayerAction(action: unknown) {
    this.socketClient.send({ type: "PLAYER_ACTION", payload: action });
  }

  requestStateSync() {
    this.socketClient.send({ type: "GAME_STATE_SYNC" });
  }

  endMatch(result: unknown) {
    this.socketClient.send({ type: "MATCH_END", payload: result });
  }
}
