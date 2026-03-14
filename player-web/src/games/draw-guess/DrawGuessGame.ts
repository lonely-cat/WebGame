import { BaseMultiplayerGame } from "../../sdk/BaseMultiplayerGame";

export class DrawGuessGame extends BaseMultiplayerGame {
  start() {}

  startRound() {}

  setDrawer(_playerId: number) {}

  submitGuess(_text: string) {}

  sendStroke(strokeData: unknown) {
    this.sendAction({ type: "DRAW_STROKE", strokeData });
  }

  applyRemoteStroke(_strokeData: unknown) {}

  endRound() {}
}
